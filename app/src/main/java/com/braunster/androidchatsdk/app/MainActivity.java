package com.braunster.androidchatsdk.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.braunster.chatsdk.Utils;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.network.BFacebookManager;
import com.facebook.widget.ProfilePictureView;

import junit.framework.Test;


public class MainActivity extends ActionBarActivity {

   private ProfilePictureView profilePictureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String sha = Utils.getSHA(this, getPackageName());

        Log.i("sss", "SHA: " + sha);

        profilePictureView = (ProfilePictureView) findViewById(R.id.profile_pic);

        BFacebookManager.loginWithFacebook(new CompletionListener() {
            @Override
            public void onDone() {
                profilePictureView.setProfileId(BFacebookManager.getUserFacebookID());
            }

            @Override
            public void onDoneWithError() {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
