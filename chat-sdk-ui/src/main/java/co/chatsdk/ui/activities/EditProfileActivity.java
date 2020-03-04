package co.chatsdk.ui.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;


import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.AvailabilityHelper;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.fragments.ProfileViewOffsetChangeListener;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ImagePickerUploader;
import co.chatsdk.ui.views.IconEditView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/14/17.
 */

public class EditProfileActivity extends BaseActivity {

    @BindView(R2.id.backdrop) ImageView backdrop;
    @BindView(R2.id.toolbar) Toolbar toolbar;
    @BindView(R2.id.titleTextView) TextView titleTextView;
    @BindView(R2.id.collapsingToolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R2.id.appbar) AppBarLayout appbar;
    @BindView(R2.id.topSpace) Space topSpace;
    @BindView(R2.id.statusTitleTextView) TextView statusTitleTextView;
    @BindView(R2.id.statusEditText) EditText statusEditText;
    @BindView(R2.id.statusLinearLayout) LinearLayout statusLinearLayout;
    @BindView(R2.id.statusCardView) CardView statusCardView;
    @BindView(R2.id.spinner) MaterialSpinner spinner;
    @BindView(R2.id.availabilityCardView) CardView availabilityCardView;
    @BindView(R2.id.nameEditView) IconEditView nameEditView;
    @BindView(R2.id.locationEditView) IconEditView locationEditView;
    @BindView(R2.id.phoneEditView) IconEditView phoneEditView;
    @BindView(R2.id.emailEditView) IconEditView emailEditView;
    @BindView(R2.id.iconLinearLayout) LinearLayout iconLinearLayout;
    @BindView(R2.id.doneFab) FloatingActionButton doneFab;
    @BindView(R2.id.logoutFab) FloatingActionButton logoutFab;
    @BindView(R2.id.avatarImageView) CircleImageView avatarImageView;
    @BindView(R2.id.root) CoordinatorLayout root;

    protected User currentUser;
    protected HashMap<String, Object> userMeta;
    protected String avatarImageURL = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userEntityID = getIntent().getStringExtra(Keys.IntentKeyUserEntityID);

        if (userEntityID == null || userEntityID.isEmpty()) {
            showToast("User Entity ID not set");
            finish();
            return;
        } else {
            currentUser = ChatSDK.db().fetchUserWithEntityID(userEntityID);

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

    protected @LayoutRes
    int getLayout() {
        return R.layout.activity_edit_profile;
    }

    protected void initViews() {
        super.initViews();

        avatarImageView.setOnClickListener(view -> {
            if (ChatSDK.profilePictures() != null) {
                ChatSDK.profilePictures().startProfilePicturesActivity(this, currentUser.getEntityID());
            } else {
                ImagePickerUploader uploader = new ImagePickerUploader(MediaSelector.CropType.Circle);
                dm.add(uploader.choosePhoto(EditProfileActivity.this, false).subscribe((results, throwable) -> {
                    if (throwable == null && !results.isEmpty()) {
                        avatarImageView.setImageURI(Uri.fromFile(new File(results.get(0).uri)));
                        avatarImageURL = results.get(0).url;
                    } else {
                        showToast(throwable.getLocalizedMessage());
                    }
                }));
            }
        });

        spinner.setItems(AvailabilityHelper.getAvailableStateStrings(this));

        appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(avatarImageView));

        backdrop.setImageResource(R.drawable.header2);

        doneFab.setImageDrawable(Icons.get(Icons.choose().check, R.color.app_bar_icon_color));
        doneFab.setOnClickListener(v -> saveAndExit());
        logoutFab.setImageDrawable(Icons.get(Icons.choose().logout, R.color.app_bar_icon_color));
        logoutFab.setOnClickListener(v -> logout());

        reloadData();
    }

    protected void reloadData() {
        // Set the current user's information
        String status = currentUser.getStatus();
        String availability = currentUser.getAvailability();
        String name = currentUser.getName();
        String location = currentUser.getLocation();
        String phoneNumber = currentUser.getPhoneNumber();
        String email = currentUser.getEmail();

        int width = Dimen.from(this, R.dimen.large_avatar_width);
        int height = Dimen.from(this, R.dimen.large_avatar_height);

        collapsingToolbar.setTitle(getString(R.string.edit_profile));
        Picasso.get().load(currentUser.getAvatarURL()).into(avatarImageView);

        currentUser.loadAvatar(avatarImageView, width, height);


        statusEditText.setText(status);

        if (availability != null && !availability.isEmpty()) {
            spinner.setSelectedIndex(AvailabilityHelper.getAvailableStates().indexOf(currentUser.getAvailability()));
        }

        nameEditView.setText(name);
        nameEditView.setNextFocusDown(R.id.locationEditView);
        nameEditView.setIcon(Icons.get(Icons.choose().user, R.color.edit_profile_icon_color));
        nameEditView.setHint(R.string.name_hint);
        nameEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        locationEditView.setText(location);
        locationEditView.setNextFocusDown(R.id.phoneEditView);
        locationEditView.setIcon(Icons.get(Icons.choose().location, R.color.edit_profile_icon_color));
        locationEditView.setHint(R.string.location_hint);
        locationEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        phoneEditView.setText(phoneNumber);
        phoneEditView.setNextFocusDown(R.id.emailEditView);
        phoneEditView.setIcon(Icons.get(Icons.choose().phone, R.color.edit_profile_icon_color));
        phoneEditView.setHint(R.string.phone_number_hint);
        phoneEditView.setInputType(InputType.TYPE_CLASS_PHONE);

        emailEditView.setText(email);
        emailEditView.setIcon(Icons.get(Icons.choose().email, R.color.edit_profile_icon_color));
        emailEditView.setHint(R.string.email_hint);
        emailEditView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    protected void logout() {
        dm.add(ChatSDK.auth().logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> ChatSDK.ui().startSplashScreenActivity(getApplicationContext()), this));
    }

    protected void saveAndExit() {

        String status = statusEditText.getText().toString().trim();

        String availability = AvailabilityHelper.getAvailableStates().get(spinner.getSelectedIndex());

        String name = nameEditView.getText();
        String location = locationEditView.getText();
        String phoneNumber = phoneEditView.getText();
        String email = emailEditView.getText();

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
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        } else {
            finished.run();
        }
    }

    protected boolean valueChanged(Map<String, Object> h1, Map<String, Object> h2, String key) {
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

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        return index;
    }

//    protected String getAvailability () {
//        String a = b.availabilitySpinner.getSelectedItem().toString().toLowerCase();
//        switch (a) {
//            case "away":
//                return Availability.Away;
//            case "extended away":
//                return Availability.XA;
//            case "busy":
//                return Availability.Busy;
//            default:
//                return Availability.Available;
//        }
//    }
//
//    protected void setAvailability (String a) {
//        String availability = "available";
//        if (a.equals(Availability.Away)) {
//            availability = "away";
//        }
//        else if (a.equals(Availability.XA)) {
//            availability = "extended away";
//        }
//        else if (a.equals(Availability.Busy)) {
//            availability = "busy";
//        }
//        b.availabilitySpinner.setSelection(getIndex(b.availabilitySpinner, availability));
//
//    }
}
