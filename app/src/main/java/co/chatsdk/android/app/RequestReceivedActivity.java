package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class RequestReceivedActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestrecieved);
    }

    //Here the user is told that their request has been received, and that they will be responded to soon.
    //Now the only thing to do is to go back to the main screen.
    public void exit_click(View v) {
        Intent i = new Intent(RequestReceivedActivity.this, WelcomeActivity.class);
        startActivity(i);
    }
}
