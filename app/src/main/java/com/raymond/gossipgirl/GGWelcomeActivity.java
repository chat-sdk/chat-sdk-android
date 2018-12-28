package com.raymond.gossipgirl;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;

public class GGWelcomeActivity extends GGAuthActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);

        User user = ChatSDK.currentUser();
        if (user != null) {
            String stageName = user.metaStringForKey(GGKeys.StageName);
            if (stageName != null && !stageName.isEmpty()) {
                ChatSDK.ui().startMainActivity(getApplicationContext());
            } else {
                ChatSDK.ui().startActivity(getApplicationContext(), GGUsernameActivity.class);
            }
        }
    }

    //Here the introductory message is displayed, and the user can start the process of signing up.

    public void didClickOnGetStarted(View v) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ChatSDK.ui().startActivity(getApplicationContext(), GGLocationActivity.class);
        } else {
            startAuthActivity();
        }
    }

}
