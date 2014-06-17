package com.braunster.chatsdk.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.tamplate.TestNetworkAdapter;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends Activity{

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private UiLifecycleHelper uiHelper;
    private LoginButton fbLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_login);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        fbLoginButton = (LoginButton) findViewById(R.id.authButton);
        fbLoginButton.setOnErrorListener(new LoginButton.OnErrorListener() {
            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "error");
            }
        });
        fbLoginButton.setReadPermissions(Arrays.asList("email", "user_friends"));

        // For registering the activity to facebook.
        String sha = Utils.getSHA(this, getPackageName());
        Log.i(TAG, "SHA: " + sha);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Auto check for session state change if session is valid.
//        Session session = Session.getActiveSession();
//        if (session != null &&
//                (session.isOpened() || session.isClosed()) ) {
//            onSessionStateChange(session, session.getState(), null);
//        }

        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        BFacebookManager.onSessionStateChange(session, state, exception, new CompletionListener() {
            @Override
            public void onDone() {
                if (DEBUG) Log.i(TAG, "FB and Firebase are connected");
                TestNetworkAdapter testNetworkAdapter = new TestNetworkAdapter();

                BNetworkManager.getInstance().setNetworkAdapter(testNetworkAdapter);
                BNetworkManager.getInstance().syncWithProgress(new CompletionListener() {
                    @Override
                    public void onDone() {

                    }

                    @Override
                    public void onDoneWithError() {

                    }
                });

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "Error connecting to FB or firebase");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Log.v(TAG, "onActivityResult");
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }
}
