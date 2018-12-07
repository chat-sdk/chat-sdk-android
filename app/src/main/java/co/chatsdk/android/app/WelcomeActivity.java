package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    //Here the introductory message is displayed, and the user can start the process of signing up.

    public void getStarted_click(View v) {
        Intent i = new Intent (WelcomeActivity.this, LocationActivity.class);
        startActivity(i);
    }
}
