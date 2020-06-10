package sdk.chat.ui.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.chat.ui.fragments.ProfileFragment;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.RX;

/**
 * Created by ben on 8/23/17.
 */

public class ProfileActivity extends BaseActivity {

    protected User user;

    @Override
    protected int getLayout() {
        return R.layout.activity_profile;
    }

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String userEntityID = getIntent().getStringExtra(Keys.IntentKeyUserEntityID);

        if (userEntityID != null && !userEntityID.isEmpty()) {
            ChatSDK.db().fetchUserWithEntityIDAsync(userEntityID).observeOn(RX.main()).doOnSuccess(user -> {
                if (user != null) {
                    ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.profileFragment);
                    fragment.setUser(user);
                } else {
                    ToastHelper.show(this, R.string.user_entity_id_not_set);
                    finish();
                }
            }).ignoreElement().subscribe(this);

//            user =  ChatSDK.db().fetchUserWithEntityID(userEntityID);
//            if (user != null) {
//                ProfileFragment fragment = (ProfileFragment) getSupportFragmentManager().findFragmentById(R.id.profileFragment);
//                fragment.setUser(user);
//                return;
//            }
        }

//        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

}
