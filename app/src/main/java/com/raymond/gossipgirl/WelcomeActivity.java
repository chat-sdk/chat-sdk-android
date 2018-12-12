package com.raymond.gossipgirl;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.session.ChatSDK;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (ChatSDK.currentUser() != null) {
            ChatSDK.ui().startMainActivity(getApplicationContext());
        }
    }

    //Here the introductory message is displayed, and the user can start the process of signing up.

    public void didClickOnGetStarted(View v) {
        ChatSDK.ui().startActivity(getApplicationContext(), LocationActivity.class);
    }

}
