package com.braunster.chatsdk.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.EditText;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BFacebookManager;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.util.List;

/**
 * Created by itzik on 6/9/2014.
 */
public class ProfileActivity extends ActionBarActivity {

    // TODO keyboard popups on screen load
    // TODO get user phone number from facebook?
    // TODO option to select profile picture.

    private static final String TAG = ProfileActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private EditText etName, etMail, etPhone;
    private ProfilePictureView profilePictureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_profile);

        initActionBar();
        initViews();

       BFacebookManager.getUserDetails(new CompletionListenerWithData<GraphUser>() {
            @Override
            public void onDone(GraphUser graphUser) {
                Log.d(TAG, "Name: " + graphUser.getName());
                etName.setText(graphUser.getName());
                etMail.setText((String) graphUser.getProperty("email"));
                profilePictureView.setProfileId(graphUser.getId());
            }

            @Override
            public void onDoneWithError() {

            }
        });

       BFacebookManager.getUserFriendList(new CompletionListenerWithData<List<GraphUser>>() {
           @Override
           public void onDone(List<GraphUser> users) {
           }

           @Override
           public void onDoneWithError() {

           }
       });
    }

    private void initActionBar(){
        getActionBar().setTitle("Profile");
    }

    private void initViews(){
        etName = (EditText) findViewById(R.id.et_name);
        etMail = (EditText) findViewById(R.id.et_mail);
        etPhone = (EditText) findViewById(R.id.et_phone_number);
        profilePictureView = (ProfilePictureView) findViewById(R.id.profile_pic);
    }
}



    /*V2 not suppose to be used.    Bundle params = new Bundle();

        params.putString("fields", "name, picture, location, installed");

        new Request(
                Session.getActiveSession(),
                "/me/friends",
                params,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        Log.d(TAG, "Completed, Response: " + response.getRawResponse());
                    }
                }
        ).executeAsync();*/