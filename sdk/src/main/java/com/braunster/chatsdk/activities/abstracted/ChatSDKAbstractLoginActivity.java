/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.activities.abstracted;

import android.content.Intent;
import android.widget.EditText;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.activities.ChatSDKBaseActivity;
import com.braunster.chatsdk.activities.ChatSDKMainActivity;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by itzik on 6/8/2014.
 */
public class ChatSDKAbstractLoginActivity extends ChatSDKBaseActivity {

    private static final String TAG = ChatSDKAbstractLoginActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.LoginActivity;

    private boolean exitOnBackPressed = false;

    protected EditText etEmail, etPass;
    protected LoginButton facebookLogin;

    /** Passed to the activity in the intent extras, Indicates that the activity was called after the user press the logout button,
     * That means the activity wont try to authenticate in inResume. */
    public static final String FLAG_LOGGED_OUT = "LoggedOut";

    protected void initViews(){
        facebookLogin = (LoginButton) findViewById(R.id.chat_sdk_facebook_button);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If there is preferences saved dont check auth ot the info does not contain AccountType.
        Map<String, ?> loginInfo =BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo();
        if (loginInfo != null && loginInfo.containsKey(BDefines.Prefs.AccountTypeKey))
            if (getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey(FLAG_LOGGED_OUT)) {

                showProgDialog(getString(R.string.authenticating));
                authenticate().done(new DoneCallback<BUser>() {
                    @Override
                    public void onDone(BUser bUser) {
                        if (DEBUG) Timber.d("Authenticated");
                        dismissProgDialog();
                        afterLogin();
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError bError) {
                        dismissProgDialog();
                        if (DEBUG) Timber.d("Auth Failed");
/*FIXME remove if not needed.                        if (bError.code != BError.Code.NO_LOGIN_INFO)
                            showAlertToast(getString(R.string.login_activity_auth_failed));*/
                    }
                });
            }
    }

    /* Dismiss dialog and open main activity.*/
    protected void afterLogin(){
        // Indexing the user.
        BUser currentUser = getNetworkAdapter().currentUserModel();
        if(getNetworkAdapter().currentUserModel() != null) {
            getNetworkAdapter().pushUser();
        }

        Intent logout = new Intent(ChatSDKMainActivity.Action_clear_data);
        sendBroadcast(logout);

        dismissProgDialog();
    }

    public void passwordLogin(){
        if (!checkFields())
            return;

        showProgDialog(getString(R.string.connecting));

        Map<String, Object> data = AbstractNetworkAdapter.getMap(
                new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey},
                BDefines.BAccountType.Password, etEmail.getText().toString(), etPass.getText().toString());

        BNetworkManager.sharedManager().getNetworkAdapter()
                .authenticateWithMap(data).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                afterLogin();
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                toastErrorMessage(bError, true);
                dismissProgDialog();
            }
        });
    }

    public void register(){
        if (!checkFields())
            return;
        showProgDialog(getString(R.string.registering));

        Map<String, Object> data = AbstractNetworkAdapter.getMap(
                new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey },
                BDefines.BAccountType.Register, etEmail.getText().toString(), etPass.getText().toString());

        BNetworkManager.sharedManager().getNetworkAdapter()
                .authenticateWithMap(data).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                afterLogin();
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                toastErrorMessage(bError, false);
                dismissProgDialog();
            }
        });
    }

    public void anonymosLogin(){
        showProgDialog(getString(R.string.connecting));

        Map<String, Object> data = new HashMap<String, Object>();
        data.put(BDefines.Prefs.LoginTypeKey, BDefines.BAccountType.Anonymous);

        BNetworkManager.sharedManager().getNetworkAdapter()
                .authenticateWithMap(data).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                afterLogin();
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                toastErrorMessage(bError, false);
                dismissProgDialog();
            }
        });
    }

    public void twitterLogin(){

        if (!BNetworkManager.sharedManager().getNetworkAdapter().twitterEnabled())
        {
            showAlertToast("Twitter is disabled.");
            return;
        }

        final DialogUtils.ChatSDKTwitterLoginDialog dialog = DialogUtils.ChatSDKTwitterLoginDialog.getInstance();
        
        dialog.promise().done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                dialog.dismiss();

                showProgDialog(getString(R.string.authenticating));

                afterLogin();
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                dialog.dismiss();
                toastErrorMessage(bError, true);
            }
        });
        
        dialog.show(getFragmentManager(), "TwitterLogin");
    }

    protected void setFacebookLogin(){
        facebookLogin.setOnErrorListener(new LoginButton.OnErrorListener() {
            @Override
            public void onError(FacebookException error) {
                if (error instanceof FacebookOperationCanceledException)
                    return;
                else if (error.getMessage() != null && error.getMessage().equals("Log in attempt aborted."))
                    return;

                showAlertToast("Facebook error: " + error.getMessage() + " " + error.getClass().getSimpleName());
            }
        });

        facebookLogin.setReadPermissions(Arrays.asList("email", "user_friends"));
    }

    /* Exit Stuff*/
    @Override
    public void onBackPressed() {
        if (exitOnBackPressed)
        {
            // Exit the app.
            // If logged out from the main activity pressing back in the LoginActivity will get me back to the Main so this have to be done.
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else super.onBackPressed();

    }

    public void toastErrorMessage(BError error, boolean login){
        String errorMessage = "";

        if (StringUtils.isNotBlank(error.message))
            errorMessage = error.message;
        else if (login)
            errorMessage = getString(R.string.login_activity_failed_to_login_toast);
        else
            errorMessage = getString(R.string.login_activity_failed_to_register_toast);


        showAlertToast(errorMessage);
    }

    protected boolean checkFields(){
        if (etEmail.getText().toString().isEmpty())
        {
            showAlertToast(getString(R.string.login_activity_no_mail_toast));
            return false;
        }

        if (etPass.getText().toString().isEmpty())
        {
            showAlertToast( getString(R.string.login_activity_no_password_toast) );
            return false;
        }

        return true;
    }

    public void onSessionStateChange(Session session, SessionState state, Exception exception){

        if (!getNetworkAdapter().facebookEnabled())
        {
            return;
        }

        if (exception != null)
        {
            exception.printStackTrace();
            if (exception instanceof FacebookOperationCanceledException)
            {
                return;
            }
        }else showOrUpdateProgDialog(getString(R.string.authenticating));

        BFacebookManager.onSessionStateChange(session, state, exception).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                if (DEBUG) Timber.i("Connected to facebook");
                afterLogin();
            }
        }).fail(new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {
                if (DEBUG) Timber.i(TAG, "Error connecting to Facebook");
//                Toast.makeText(LoginActivity.this, "Failed connect to facebook.", Toast.LENGTH_SHORT).show();
                showAlertToast( getString(R.string.login_activity_facebook_connection_fail_toast) );
                BFacebookManager.logout(ChatSDKAbstractLoginActivity.this);
                dismissProgDialog();
            }
        });
    }

    protected void setExitOnBackPressed(boolean exitOnBackPressed) {
        this.exitOnBackPressed = exitOnBackPressed;
    }
}
