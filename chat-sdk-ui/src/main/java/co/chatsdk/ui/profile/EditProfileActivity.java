package co.chatsdk.ui.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.mukesh.countrypicker.CountryPicker;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.LayoutRes;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.ImagePickerUploader;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/14/17.
 */

public class EditProfileActivity extends BaseActivity {

    protected SimpleDraweeView avatarImageView;
    protected String avatarImageURL = null;

    protected EditText statusEditText;
    protected Spinner availabilitySpinner;
    protected EditText nameEditText;
    protected EditText locationEditText;
    protected EditText phoneNumberEditText;
    protected EditText emailEditText;
    protected Button countryButton;
    protected Button logoutButton;
    protected HashMap<String, Object> userMeta;

    protected User currentUser;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userEntityID = getIntent().getStringExtra(Keys.IntentKeyUserEntityID);

        if (userEntityID == null || userEntityID.isEmpty()) {
            showToast("User Entity ID not set");
            finish();
            return;
        }
        else {
            currentUser =  ChatSDK.db().fetchUserWithEntityID(userEntityID);

            // Save a copy of the data to see if it has changed
            userMeta = new HashMap<>(currentUser.metaMap());
        }

        disposableList.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkEvent -> {
                    if (networkEvent.user.equals(currentUser)) {
                        reloadData();
                    }
                }));

        setContentView(activityLayout());

        initViews();
    }

    protected @LayoutRes int activityLayout() {
        return R.layout.edit_profile;
    }

    protected void initViews() {
        avatarImageView = findViewById(R.id.image_avatar);
        statusEditText = findViewById(R.id.etStatus);
        availabilitySpinner = findViewById(R.id.spAvailability);
        nameEditText = findViewById(R.id.etName);
        locationEditText = findViewById(R.id.etLocation);
        phoneNumberEditText = findViewById(R.id.etPhone);
        emailEditText = findViewById(R.id.etEmail);

        countryButton = findViewById(R.id.btnCountry);
        logoutButton = findViewById(R.id.btnLogout);

        avatarImageView.setOnClickListener(view -> {
            if (ChatSDK.profilePictures() != null) {
                ChatSDK.profilePictures().startProfilePicturesActivity(this, currentUser.getEntityID());
            } else {
                ImagePickerUploader uploader = new ImagePickerUploader(MediaSelector.CropType.Circle);
                disposableList.add(uploader.choosePhoto(EditProfileActivity.this).subscribe((result, throwable) -> {
                    if (throwable == null) {
                        avatarImageView.setImageURI(Uri.fromFile(new File(result.uri)));
                        avatarImageURL = result.url;
                    } else {
                        ToastHelper.show(EditProfileActivity.this, throwable.getLocalizedMessage());
                    }
                }));
            }
        });

        countryButton.setOnClickListener(view -> {

            final CountryPicker picker = new CountryPicker.Builder().with(EditProfileActivity.this).listener(country -> {
                countryButton.setText(country.getName());
                currentUser.setCountryCode(country.getCode());
            }).build();

            picker.showDialog(EditProfileActivity.this);

        });

        logoutButton.setOnClickListener(view -> logout());

        reloadData();
    }

    protected void reloadData () {
        // Set the current user's information
        String status = currentUser.getStatus();
        String availability = currentUser.getAvailability();
        String name = currentUser.getName();
        String location = currentUser.getLocation();
        String phoneNumber = currentUser.getPhoneNumber();
        String email = currentUser.getEmail();
        String countryCode = currentUser.getCountryCode();

        avatarImageView.setImageURI(currentUser.getAvatarURL());

        if (StringUtils.isNotEmpty(countryCode)) {
            Locale l = new Locale("", countryCode);
            countryButton.setText(l.getDisplayCountry());
        }

        statusEditText.setText(status);

        if (!StringUtils.isEmpty(availability)) {
            setAvailability(availability);
        }

        nameEditText.setText(name);
        locationEditText.setText(location);
        phoneNumberEditText.setText(phoneNumber);
        emailEditText.setText(email);
    }

    protected void logout () {
        disposableList.add(ChatSDK.auth().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> ChatSDK.ui().startSplashScreenActivity(getApplicationContext()), throwable -> {
            ChatSDK.logError(throwable);
            Toast.makeText(EditProfileActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item = menu.add(Menu.NONE, R.id.action_chat_sdk_save, 12, getString(R.string.action_save));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icn_24_save);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_save) {
            saveAndExit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void saveAndExit () {

        String status = statusEditText.getText().toString().trim();
        String availability = getAvailability().trim();
        String name = nameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        currentUser.setStatus(status);
        currentUser.setAvailability(availability);
        currentUser.setName(name);
        currentUser.setLocation(location);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setEmail(email);

        boolean changed = !userMeta.entrySet().equals(currentUser.metaMap().entrySet());
//        boolean imageChanged = false;
        boolean presenceChanged = false;

        Map<String, Object> metaMap = new HashMap<>(currentUser.metaMap());

        // Add a synchronized block to prevent concurrent modification exceptions
        for (String key : metaMap.keySet()) {
            if (key.equals(Keys.AvatarURL)) {
//                imageChanged = valueChanged(metaMap, userMeta, key);
                currentUser.setAvatarHash(null);
            }
            if (key.equals(Keys.Availability) || key.equals(Keys.Status)) {
                presenceChanged = presenceChanged || valueChanged(metaMap, userMeta, key);
            }
        }

        if (avatarImageURL != null) {
            currentUser.setAvatarURL(avatarImageURL);
        }

        currentUser.update();

        if (presenceChanged && !changed) {
            // Send presence
            ChatSDK.core().goOnline();
        }

        // TODO: Add this in for Firebase maybe move this to push user...
//        if (imageChanged && avatarURL != null) {
//            UserAvatarHelper.saveProfilePicToServer(avatarURL, this).subscribe();
//        }
//        else if (changed) {

        final Runnable finished = () -> {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            finish();
        };


        if (changed) {
            showOrUpdateProgressDialog(getString(R.string.alert_save_contact));
            disposableList.add(ChatSDK.core().pushUser()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        dismissProgressDialog();
                        finished.run();
                    }));
        }
        else {
            finished.run();
        }
    }

    protected boolean valueChanged (Map<String, Object> h1, Map<String, Object> h2, String key) {
        Object o1 = h1.get(key);
        Object o2 = h2.get(key);
        if (o1 == null) {
            return o2 != null;
        } else {
            return !o1.equals(o2);
        }
    }

    protected int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i<spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

    protected String getAvailability () {
        String a = availabilitySpinner.getSelectedItem().toString().toLowerCase();
        switch (a) {
            case "away":
                return Availability.Away;
            case "extended away":
                return Availability.XA;
            case "busy":
                return Availability.Busy;
            default:
                return Availability.Available;
        }
    }

    protected void setAvailability (String a) {
        String availability = "available";
        if (a.equals(Availability.Away)) {
            availability = "away";
        }
        else if (a.equals(Availability.XA)) {
            availability = "extended away";
        }
        else if (a.equals(Availability.Busy)) {
            availability = "busy";
        }
        availabilitySpinner.setSelection(getIndex(availabilitySpinner, availability));

    }

}
