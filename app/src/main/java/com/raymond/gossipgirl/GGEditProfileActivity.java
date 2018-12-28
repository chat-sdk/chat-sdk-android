package com.raymond.gossipgirl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.chat.MediaSelector;
import co.chatsdk.ui.profile.EditProfileActivity;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class GGEditProfileActivity extends EditProfileActivity {

    private DisposableList disposableList = new DisposableList();

    @Override
    protected int activityLayout() {
        return R.layout.activity_edit_profile;
    }

    @Override
    protected void initViews() {
        avatarImageView = findViewById(co.chatsdk.ui.R.id.ivAvatar);
        statusEditText = findViewById(co.chatsdk.ui.R.id.etStatus);
        availabilitySpinner = findViewById(co.chatsdk.ui.R.id.spAvailability);
        phoneNumberEditText = findViewById(co.chatsdk.ui.R.id.etPhone);
        emailEditText = findViewById(co.chatsdk.ui.R.id.etEmail);

        logoutButton = findViewById(co.chatsdk.ui.R.id.btnLogout);

        // Set the current user's information
        String status = currentUser.getStatus();
        String availability = currentUser.getAvailability();
        String phoneNumber = currentUser.getPhoneNumber();
        String email = currentUser.getEmail();

        avatarImageView.setOnClickListener(view -> {
            if (ChatSDK.profilePictures() != null) {
                ChatSDK.profilePictures().startProfilePicturesActivity(this, currentUser.getEntityID());
            } else {
                mediaSelector.startChooseImageActivity(GGEditProfileActivity.this, MediaSelector.CropType.Circle, result -> {

                    try {
                        File compress = new Compressor(ChatSDK.shared().context())
                                .setMaxHeight(ChatSDK.config().imageMaxThumbnailDimension)
                                .setMaxWidth(ChatSDK.config().imageMaxThumbnailDimension)
                                .compressToFile(new File(result));

                        Bitmap bitmap = BitmapFactory.decodeFile(compress.getPath());

                        // Cache the file
                        File file = ImageUtils.compressImageToFile(ChatSDK.shared().context(), bitmap, ChatSDK.currentUser().getEntityID(), ".png");

                        avatarImageView.setImageURI(Uri.fromFile(file));
                        currentUser.setAvatarURL(Uri.fromFile(file).toString());
                    }
                    catch (Exception e) {
                        ChatSDK.logError(e);
                        Toast.makeText(GGEditProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        avatarImageView.setImageURI(currentUser.getAvatarURL());

        logoutButton.setOnClickListener(view -> logout());

        statusEditText.setText(status);

        if (!StringUtils.isEmpty(availability)) {
            setAvailability(availability);
        }

        phoneNumberEditText.setText(phoneNumber);
        emailEditText.setText(email);
    }

    @Override
    protected void saveAndExit() {
        String status = statusEditText.getText().toString().trim();
        String availability = getAvailability().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        currentUser.setStatus(status);
        currentUser.setAvailability(availability);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setEmail(email);

        boolean changed = !userMeta.equals(currentUser.metaMap());
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
            showOrUpdateProgressDialog(getString(co.chatsdk.ui.R.string.alert_save_contact));
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

    @Override
    protected void onStop() {
        super.onStop();
        disposableList.dispose();
    }

}
