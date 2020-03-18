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


import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.materialspinner.MaterialSpinner;

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

    @BindView(R2.id.headerImageView) protected ImageView headerImageView;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.titleTextView) protected TextView titleTextView;
    @BindView(R2.id.collapsingToolbar) protected CollapsingToolbarLayout collapsingToolbar;
    @BindView(R2.id.appbar) protected AppBarLayout appbar;
    @BindView(R2.id.topSpace) protected Space topSpace;
    @BindView(R2.id.statusTitleTextView) protected TextView statusTitleTextView;
    @BindView(R2.id.statusEditText) protected EditText statusEditText;
    @BindView(R2.id.statusLinearLayout) protected LinearLayout statusLinearLayout;
    @BindView(R2.id.statusCardView) protected CardView statusCardView;
    @BindView(R2.id.spinner) protected MaterialSpinner spinner;
    @BindView(R2.id.availabilityCardView) protected CardView availabilityCardView;
    @BindView(R2.id.nameEditView) protected IconEditView nameEditView;
    @BindView(R2.id.locationEditView) protected IconEditView locationEditView;
    @BindView(R2.id.phoneEditView) protected IconEditView phoneEditView;
    @BindView(R2.id.emailEditView) protected IconEditView emailEditView;
    @BindView(R2.id.iconLinearLayout) protected LinearLayout iconLinearLayout;
    @BindView(R2.id.doneFab) protected FloatingActionButton doneFab;
    @BindView(R2.id.logoutFab) protected FloatingActionButton logoutFab;
    @BindView(R2.id.avatarImageView) protected CircleImageView avatarImageView;
    @BindView(R2.id.root) protected CoordinatorLayout root;

    protected User currentUser;
    protected HashMap<String, Object> userMeta;
    protected String avatarImageURL = null;
    protected String headerImageURL = null;

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
                dm.add(uploader.choosePhoto(EditProfileActivity.this, false).subscribe(results -> {
                    avatarImageView.setImageURI(Uri.fromFile(new File(results.get(0).uri)));
                    avatarImageURL = results.get(0).url;
                }, this));
            }
        });

        spinner.setItems(AvailabilityHelper.getAvailableStateStrings(this));

        appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(avatarImageView));
        appbar.setOnClickListener(v -> {
            ImagePickerUploader uploader = new ImagePickerUploader(MediaSelector.CropType.None);
            dm.add(uploader.choosePhoto(EditProfileActivity.this, false).subscribe(results -> {
                setHeaderImage(results.get(0).url);
                headerImageURL = results.get(0).url;
            }, this));
        });

        doneFab.setImageDrawable(Icons.get(Icons.choose().check, R.color.app_bar_icon_color));
        doneFab.setOnClickListener(v -> saveAndExit());
        logoutFab.setImageDrawable(Icons.get(Icons.choose().logout, R.color.app_bar_icon_color));
        logoutFab.setOnClickListener(v -> logout());

        reloadData();
    }

    protected void setHeaderImage(@Nullable String url) {
        // Make sure that this runs when the view has dimensions
        root.post(() -> {
            int profileHeader = ChatSDK.config().profileHeaderImage;
            if (url != null && appbar != null) {
                // Get the screen width
                Glide.with(this)
                        .load(url)
                        .dontAnimate()
                        .override(appbar.getWidth(), appbar.getHeight())
                        .centerCrop()
                        .placeholder(profileHeader)
                        .error(R.drawable.header)
                        .into(headerImageView);
            } else {
                headerImageView.setImageResource(profileHeader);
            }
        });
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
        Glide.with(this).load(currentUser.getAvatarURL()).dontAnimate().placeholder(ChatSDK.ui().getDefaultProfileImage()).into(avatarImageView);

        currentUser.loadAvatar(avatarImageView, width, height);

        setHeaderImage(currentUser.getHeaderURL());

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
                .subscribe(() -> ChatSDK.ui().startSplashScreenActivity(this), this));
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
        boolean avatarChanged = avatarImageURL != null && !avatarImageURL.equals(currentUser.getAvatarURL());

        // If this is a new avatar, reset the hash code, this will prompt
        // the XMPP client to update the image
        if (avatarChanged) {
            currentUser.setAvatarURL(avatarImageURL, null);
        }

        if (headerImageURL != null) {
            currentUser.setHeaderURL(headerImageURL);
        }

        final Runnable finished = () -> {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            finish();
        };

        if (changed || avatarChanged) {

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

}
