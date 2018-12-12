package com.raymond.gossipgirl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.session.ChatSDK;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class GGConfirmUsernameActivity extends AppCompatActivity {

    private String username;
    private String stageName;
    private String presentedStageName;
    private String city;
    private TextView confirmUsername;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_username);

        //Retrieve the data from the last activity
        Intent i = getIntent();
        username = (String) i.getSerializableExtra(GGKeys.Username);
        stageName = (String) i.getSerializableExtra(GGKeys.StageName);
        presentedStageName = (String) i.getSerializableExtra(GGKeys.PresentedStageName);
        city = (String) i.getSerializableExtra(GGKeys.City);

        //The confirmUsername is the same as the presentedStageName, it just needs to be written on two lines.
        confirmUsername = (TextView) findViewById(R.id.confirm_username);
        confirmUsername.setText(username + "\n" + city);
    }

    public void didClickOnContinue(View v) {

        //If the user clicks this we start the main activity and write their stage name, city and presented stage name to firebase.
        ChatSDK.currentUser().setMetaString(GGKeys.PresentedStageName, presentedStageName);
        ChatSDK.currentUser().setMetaString(GGKeys.StageName, stageName);
        ChatSDK.currentUser().setMetaString(GGKeys.City, city);
        ChatSDK.core().pushUser().subscribe(new Action() {
            @Override
            public void run() throws Exception {

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

            }
        });

        ChatSDK.ui().startMainActivity(GGConfirmUsernameActivity.this);
    }

    public void didClickOnBack(View v) {
        Intent i = new Intent (GGConfirmUsernameActivity.this, GGUsernameActivity.class);
        startActivity(i);
    }

}
