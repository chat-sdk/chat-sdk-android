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

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.MediaSelector;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ImagePickerUploader;
import sdk.chat.ui.utils.UserImageBuilder;
import sdk.chat.ui.views.IconEditView;
import sdk.guru.common.RX;

public class PostRegistrationActivity extends BaseActivity {

    @BindView(R2.id.avatarImageView) CircleImageView avatarImageView;
    @BindView(R2.id.nameEditView) IconEditView nameEditView;
    @BindView(R2.id.locationEditView) IconEditView locationEditView;
    @BindView(R2.id.phoneEditView) IconEditView phoneEditView;
    @BindView(R2.id.emailEditView) IconEditView emailEditView;
    @BindView(R2.id.iconLinearLayout) LinearLayout iconLinearLayout;
    @BindView(R2.id.doneFab) FloatingActionButton doneFab;
    @BindView(R2.id.root) RelativeLayout root;

    String avatarImageURL = null;

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

        avatarImageView.setOnClickListener(view -> {
            ImagePickerUploader uploader = new ImagePickerUploader(MediaSelector.CropType.Circle);
            dm.add(uploader.choosePhoto(this, false).subscribe(results -> {
                avatarImageView.setImageURI(Uri.fromFile(new File(results.get(0).uri)));
                avatarImageURL = results.get(0).url;
            }, this));
        });

        doneFab.setImageDrawable(Icons.get(this, Icons.choose().check, Icons.shared().actionBarIconColor));
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
            showToast(R.string.name_field_must_be_set);
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

        currentUser.update();

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
        nameEditView.setIcon(Icons.get(this, Icons.choose().user, R.color.edit_profile_icon_color));
        nameEditView.setHint(R.string.name_hint_required);
        nameEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        locationEditView.setText(location);
        locationEditView.setNextFocusDown(R.id.phoneEditView);
        locationEditView.setIcon(Icons.get(this, Icons.choose().location, R.color.edit_profile_icon_color));
        locationEditView.setHint(R.string.location_hint);
        locationEditView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        phoneEditView.setText(phoneNumber);
        phoneEditView.setNextFocusDown(R.id.emailEditView);
        phoneEditView.setIcon(Icons.get(this, Icons.choose().phone, R.color.edit_profile_icon_color));
        phoneEditView.setHint(R.string.phone_number_hint);
        phoneEditView.setInputType(InputType.TYPE_CLASS_PHONE);

        emailEditView.setText(email);
        emailEditView.setIcon(Icons.get(this, Icons.choose().email, R.color.edit_profile_icon_color));
        emailEditView.setHint(R.string.email_hint);
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
