package co.chatsdk.ui.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.LayoutRes;
import androidx.databinding.DataBindingUtil;

import com.squareup.picasso.Picasso;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.databinding.ActivityEditProfileBinding;
import co.chatsdk.ui.fragments.ProfileViewOffsetChangeListener;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ImagePickerUploader;
import co.chatsdk.ui.views.IconEditView;
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
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());

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
        super.initViews();

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
                        showToast(throwable.getLocalizedMessage());
                    }
                }));
            }
        });

        b.appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(b.avatarImageView));

        b.backdrop.setImageResource(R.drawable.header2);

        b.doneFab.setImageDrawable(Icons.get(Icons.shared().check, R.color.app_bar_icon_color));
        b.doneFab.setOnClickListener(v -> saveAndExit());
        b.logoutFab.setImageDrawable(Icons.get(Icons.shared().logout, R.color.app_bar_icon_color));
        b.logoutFab.setOnClickListener(v -> logout());

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

        int width = Dimen.from(this, R.dimen.large_avatar_width);
        int height = Dimen.from(this, R.dimen.large_avatar_height);

        b.collapsingToolbar.setTitle(getString(R.string.edit_profile));
        Picasso.get().load(currentUser.getAvatarURL()).into(b.avatarImageView);

        currentUser.loadAvatar(b.avatarImageView, width, height);


        b.statusEditText.setText(status);

        if (availability != null && !availability.isEmpty()) {
            setAvailability(availability);
        }

        b.nameEditView.setText(name);
        b.nameEditView.setNextFocusDown(R.id.locationEditView);
        b.nameEditView.setIcon(Icons.get(Icons.shared().user, R.color.edit_profile_icon_color));
        b.nameEditView.setHint(R.string.name_hint);
        b.nameEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        b.locationEditView.setText(location);
        b.locationEditView.setNextFocusDown(R.id.phoneEditView);
        b.locationEditView.setIcon(Icons.get(Icons.shared().location, R.color.edit_profile_icon_color));
        b.locationEditView.setHint(R.string.location_hint);
        b.locationEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        b.phoneEditView.setText(phoneNumber);
        b.phoneEditView.setNextFocusDown(R.id.emailEditView);
        b.phoneEditView.setIcon(Icons.get(Icons.shared().phone, R.color.edit_profile_icon_color));
        b.phoneEditView.setHint(R.string.phone_number_hint);
        b.phoneEditView.setInputType(InputType.TYPE_CLASS_PHONE);

        b.emailEditView.setText(email);
        b.emailEditView.setIcon(Icons.get(Icons.shared().email, R.color.edit_profile_icon_color));
        b.emailEditView.setHint(R.string.email_hint);
        b.emailEditView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    protected void logout () {
        dm.add(ChatSDK.auth().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> ChatSDK.ui().startSplashScreenActivity(getApplicationContext()), this));
    }

    protected void saveAndExit () {

        String status = b.statusEditText.getText().toString().trim();
        String availability = getAvailability().trim();

        String name = b.nameEditView.getText();
        String location = b.locationEditView.getText();
        String phoneNumber = b.phoneEditView.getText();
        String email = b.emailEditView.getText();

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
