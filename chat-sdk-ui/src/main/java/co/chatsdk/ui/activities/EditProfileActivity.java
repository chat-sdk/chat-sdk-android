package co.chatsdk.ui.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial;
import com.mikepenz.iconics.typeface.library.materialdesigndx.MaterialDesignDx;
import com.mukesh.countrypicker.CountryPicker;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;

import butterknife.BindView;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.databinding.ActivityEditProfileBinding;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ImagePickerUploader;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/14/17.
 */

public class EditProfileActivity extends BaseActivity {

    protected User currentUser;
    protected HashMap<String, Object> userMeta;
    protected String avatarImageURL = null;

    protected ActivityEditProfileBinding b;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        b = DataBindingUtil.setContentView(this, getLayout());
        super.onCreate(savedInstanceState);
        initViews();

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

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkEvent -> {
                    if (networkEvent.user.equals(currentUser)) {
                        reloadData();
                    }
                }));

        initViews();
    }

    protected @LayoutRes int getLayout() {
        return R.layout.activity_edit_profile;
    }

    protected void initViews() {

        b.locationImageView.setImageDrawable(Icons.get(Icons.shared().location, R.color.edit_profile_icon_color));
        b.nameImageView.setImageDrawable(Icons.get(Icons.shared().user, R.color.edit_profile_icon_color));
        b.phoneImageView.setImageDrawable(Icons.get(Icons.shared().phone, R.color.edit_profile_icon_color));
        b.emailImageView.setImageDrawable(Icons.get(Icons.shared().email, R.color.edit_profile_icon_color));

        b.avatarImageView.setOnClickListener(view -> {
            if (ChatSDK.profilePictures() != null) {
                ChatSDK.profilePictures().startProfilePicturesActivity(this, currentUser.getEntityID());
            } else {
                ImagePickerUploader uploader = new ImagePickerUploader(MediaSelector.CropType.Circle);
                dm.add(uploader.choosePhoto(EditProfileActivity.this, false).subscribe((results, throwable) -> {
                    if (throwable == null && !results.isEmpty()) {
                        b.avatarImageView.setImageURI(Uri.fromFile(new File(results.get(0).uri)));
                        avatarImageURL = results.get(0).url;
                    } else {
                        ToastHelper.show(EditProfileActivity.this, throwable.getLocalizedMessage());
                    }
                }));
            }
        });

        b.countryButton.setOnClickListener(view -> {

            final CountryPicker picker = new CountryPicker.Builder().with(EditProfileActivity.this).listener(country -> {
                b.countryButton.setText(country.getName());
                currentUser.setCountryCode(country.getCode());
            }).build();

            picker.showDialog(EditProfileActivity.this);

        });

        b.logoutButton.setOnClickListener(view -> logout());

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

        int width = Dimen.from(this, R.dimen.large_avatar_width);
        int height = Dimen.from(this, R.dimen.large_avatar_height);

        currentUser.loadAvatar(b.avatarImageView, width, height);

        if (countryCode != null && !countryCode.isEmpty()) {
            Locale l = new Locale("", countryCode);
            b.countryButton.setText(l.getDisplayCountry());
        }

        b.statusEditText.setText(status);

        if (availability != null && !availability.isEmpty()) {
            setAvailability(availability);
        }

        b.nameEditText.setText(name);
        b.locationEditText.setText(location);
        b.phoneEditText.setText(phoneNumber);
        b.emailEditText.setText(email);
    }

    protected void logout () {
        dm.add(ChatSDK.auth().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> ChatSDK.ui().startSplashScreenActivity(getApplicationContext()), this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem item = menu.add(Menu.NONE, R.id.action_save, 12, getString(R.string.action_save));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(Icons.get(Icons.shared().check, R.color.app_bar_icon_color));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveAndExit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void saveAndExit () {

        String status = b.statusEditText.getText().toString().trim();
        String availability = getAvailability().trim();
        String name = b.nameEditText.getText().toString().trim();
        String location = b.locationEditText.getText().toString().trim();
        String phoneNumber = b.phoneEditText.getText().toString().trim();
        String email = b.emailEditText.getText().toString().trim();

        currentUser.setStatus(status);
        currentUser.setAvailability(availability);
        currentUser.setName(name);
        currentUser.setLocation(location);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setEmail(email);

        boolean changed = !userMeta.entrySet().equals(currentUser.metaMap().entrySet());
        boolean presenceChanged = false;

        Map<String, Object> metaMap = new HashMap<>(currentUser.metaMap());

        for (String key : metaMap.keySet()) {
            if (key.equals(Keys.AvatarURL)) {
                currentUser.setAvatarHash(null);
            }
            if (key.equals(Keys.Availability) || key.equals(Keys.Status)) {
                presenceChanged = presenceChanged || valueChanged(metaMap, userMeta, key);
            }
        }

        if (avatarImageURL != null) {
            currentUser.setAvatarURL(avatarImageURL);
        }

        if (presenceChanged && !changed) {
            // Send presence
            ChatSDK.core().goOnline();
        }

        final Runnable finished = () -> {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            finish();
        };

        if (changed) {

            currentUser.update();

            showOrUpdateProgressDialog(getString(R.string.alert_save_contact));
            dm.add(ChatSDK.core().pushUser()
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
        String a = b.availabilitySpinner.getSelectedItem().toString().toLowerCase();
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
        b.availabilitySpinner.setSelection(getIndex(b.availabilitySpinner, availability));

    }

}
