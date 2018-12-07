package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class SocialLogin extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);
    }

    public void continue_click(View v) {
        Intent i = new Intent (SocialLogin.this, GossipGirlUsernameActivity.class);
        startActivity(i);
    }
}
