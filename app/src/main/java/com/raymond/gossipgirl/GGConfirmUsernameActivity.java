package com.raymond.gossipgirl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mukesh.countrypicker.Country;
import com.mukesh.countrypicker.CountryPicker;

import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.utils.ToastHelper;

public class GGConfirmUsernameActivity extends AppCompatActivity {

    private String username;
    private String stageName;
    private String presentedStageName;
    private String countryName;
    private String location;

    private DisposableList disposableList = new DisposableList();
    private CountryPicker countryPicker = new CountryPicker.Builder().build();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_username);

        //Retrieve the data from the last activity
        Intent i = getIntent();
        username = i.getStringExtra(GGKeys.Username);
        stageName = i.getStringExtra(GGKeys.StageName);
        presentedStageName = i.getStringExtra(GGKeys.PresentedStageName);
        countryName = i.getStringExtra(GGKeys.Country);
        location = i.getStringExtra(Keys.Location);

        //The confirmUsername is the same as the presentedStageName, it just needs to be written on two lines.
        TextView confirmUsername = findViewById(R.id.confirm_username);
        confirmUsername.setText(username + "\n" + location);
    }

    public void didClickOnContinue(View v) {

        //If the user clicks this we start the main activity and write their stage name, city and presented stage name to firebase.
        ChatSDK.currentUser().setName(username);
        ChatSDK.currentUser().setLocation(location);
        ChatSDK.currentUser().setMetaString(GGKeys.PresentedStageName, presentedStageName);
        ChatSDK.currentUser().setMetaString(GGKeys.StageName, stageName);
        Country country = countryPicker.getCountryByName(countryName);
        if (country != null) {
            ChatSDK.currentUser().setCountryCode(country.getCode());
        } else {
            ChatSDK.currentUser().setCountryCode(countryName);
        }
        disposableList.add(ChatSDK.core().pushUser().subscribe(() -> {
            ChatSDK.ui().startMainActivity(GGConfirmUsernameActivity.this);
        }, throwable -> {
            ToastHelper.show(getApplicationContext(), throwable.getLocalizedMessage());
        }));
    }

    public void didClickOnBack(View v) {
        Intent i = new Intent (GGConfirmUsernameActivity.this, GGUsernameActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposableList.dispose();
    }
}
