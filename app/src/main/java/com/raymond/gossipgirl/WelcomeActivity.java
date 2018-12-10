package com.raymond.gossipgirl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getSupportActionBar().hide();
    }

    //Here the introductory message is displayed, and the user can start the process of signing up.

    public void getStarted_click(View v) {
        Intent i = new Intent (WelcomeActivity.this, LocationActivity.class);
        startActivity(i);
    }

}
