package sdk.chat.ui.activities;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ImagePickerUploader;
import sdk.chat.ui.utils.UserImageBuilder;
import sdk.chat.ui.views.IconEditView;
import sdk.guru.common.RX;

public class PostRegistrationActivity extends BaseActivity {

    protected CircleImageView avatarImageView;
    protected IconEditView nameEditView;
    protected IconEditView locationEditView;
    protected IconEditView phoneEditView;
    protected IconEditView emailEditView;
    protected LinearLayout iconLinearLayout;
    protected FloatingActionButton doneFab;
    protected RelativeLayout root;

    protected String avatarImageURL = null;

    @Override
    protected int getLayout() {
        return R.layout.activity_post_registration;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();
    }

    protected void initViews() {
        super.initViews();

        avatarImageView = findViewById(R.id.avatarImageView);
        nameEditView = findViewById(R.id.nameEditView);
        locationEditView = findViewById(R.id.locationEditView);
        phoneEditView = findViewById(R.id.phoneEditView);
        emailEditView = findViewById(R.id.emailEditView);
        iconLinearLayout = findViewById(R.id.iconLinearLayout);
        doneFab = findViewById(R.id.doneFab);
        root = findViewById(R.id.root);

        avatarImageView.setOnClickListener(view -> {
            ImagePickerUploader uploader = new ImagePickerUploader();
            dm.add(uploader.chooseCircularPhoto(contract, ChatSDK.config().imageMaxThumbnailDimension).subscribe(results -> {
                avatarImageView.setImageURI(Uri.fromFile(new File(results.get(0).uri)));
                avatarImageURL = results.get(0).url;
            }, this));
        });

        doneFab.setImageDrawable(ChatSDKUI.icons().get(this, ChatSDKUI.icons().check, ChatSDKUI.icons().actionBarIconColor));
        doneFab.setOnClickListener(v -> {
            doneFab.setEnabled(false);
            next();
        });

        reloadData();
    }

    protected void next() {

        User currentUser = ChatSDK.currentUser();

        String name = nameEditView.getText();
        String location = locationEditView.getText();
        String phoneNumber = phoneEditView.getText();
        String email = emailEditView.getText();

        if (StringChecker.isNullOrEmpty(name)) {
            showToast(sdk.chat.core.R.string.name_field_must_be_set);
            return;
        }

        currentUser.setName(name, false);
        currentUser.setLocation(location, false);
        currentUser.setPhoneNumber(phoneNumber, false);
        currentUser.setEmail(email, false);

        // If this is a new avatar, reset the hash code, this will prompt
        // the XMPP client to update the image
        if (avatarImageURL != null) {
            currentUser.setAvatarURL(avatarImageURL, null, false);
        }

        ChatSDK.db().update(currentUser);

        View v = getCurrentFocus();
        if (v instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            ChatSDK.events().source().accept(NetworkEvent.userMetaUpdated(currentUser));
        }

        dm.add(ChatSDK.core().pushUser()
                .observeOn(RX.main())
                .subscribe(() -> {
                    ChatSDK.ui().startMainActivity(this);
                }));
    }

    protected void reloadData() {

        User currentUser = ChatSDK.currentUser();

        String name = currentUser.getName();
        String location = currentUser.getLocation();
        String phoneNumber = currentUser.getPhoneNumber();
        String email = currentUser.getEmail();

        int width = Dimen.from(this, R.dimen.large_avatar_width);
        int height = Dimen.from(this, R.dimen.large_avatar_height);

        Glide.with(this).load(currentUser.getAvatarURL()).dontAnimate().placeholder(UIModule.config().defaultProfilePlaceholder).into(avatarImageView);

        UserImageBuilder.loadAvatar(currentUser, avatarImageView, width, height);

        nameEditView.setText(name);
        nameEditView.setNextFocusDown(R.id.locationEditView);
        nameEditView.setIcon(ChatSDKUI.icons().get(this, ChatSDKUI.icons().user, R.color.edit_profile_icon_color));
        nameEditView.setHint(sdk.chat.core.R.string.name_hint_required);
        nameEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        locationEditView.setText(location);
        locationEditView.setNextFocusDown(R.id.phoneEditView);
        locationEditView.setIcon(ChatSDKUI.icons().get(this, ChatSDKUI.icons().location, R.color.edit_profile_icon_color));
        locationEditView.setHint(sdk.chat.core.R.string.location_hint);
        locationEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        phoneEditView.setText(phoneNumber);
        phoneEditView.setNextFocusDown(R.id.emailEditView);
        phoneEditView.setIcon(ChatSDKUI.icons().get(this, ChatSDKUI.icons().phone, R.color.edit_profile_icon_color));
        phoneEditView.setHint(sdk.chat.core.R.string.phone_number_hint);
        phoneEditView.setInputType(InputType.TYPE_CLASS_PHONE);

        emailEditView.setText(email);
        emailEditView.setIcon(ChatSDKUI.icons().get(this, ChatSDKUI.icons().email, R.color.edit_profile_icon_color));
        emailEditView.setHint(sdk.chat.core.R.string.email_hint);
        emailEditView.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        doneFab.setEnabled(true);
    }

}
