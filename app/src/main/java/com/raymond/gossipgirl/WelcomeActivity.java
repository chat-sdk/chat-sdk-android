package com.raymond.gossipgirl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    //Here the introductory message is displayed, and the user can start the process of signing up.

    public void didClickOnGetStarted(View v) {
        Intent i = new Intent (WelcomeActivity.this, LocationActivity.class);
        startActivity(i);
    }

}
