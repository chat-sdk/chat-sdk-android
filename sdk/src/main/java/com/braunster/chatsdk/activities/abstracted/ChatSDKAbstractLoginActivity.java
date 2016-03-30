package com.braunster.chatsdk.activities.abstracted;

import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.activities.ChatSDKBaseActivity;
import com.braunster.chatsdk.activities.ChatSDKMainActivity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
                authenticate(
                        new AuthListener() {
                            @Override
                            public void onCheckDone(boolean isAuthenticated) {
                                if (isAuthenticated) {
                                    if (DEBUG) Log.d(TAG, "Authenticated");
                                } else {
                                    dismissProgDialog();
                                    if (DEBUG) Log.d(TAG, "Not Authenticated");
                                }
                            }

                            @Override
                            public void onLoginDone() {
                                if (DEBUG) Log.d(TAG, "Login Done");
                                afterLogin();
                            }

                            @Override
                            public void onLoginFailed(BError error) {
                                dismissProgDialog();

                                if (error.code != BError.Code.NO_LOGIN_INFO)
                                    showAlertToast(getString(R.string.login_activity_auth_failed));
                            }
                        }
                );
            }
    }

    /* Dismiss dialog and open main activity.*/
    protected void afterLogin(){
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
                .authenticateWithMap(data, new CompletionListenerWithDataAndError<Object, BError>() {
                    @Override
                    public void onDone(Object authData) {
                        afterLogin();
                    }

                    @Override
                    public void onDoneWithError(Object authData, BError o) {
                        toastErrorMessage(o, true);

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
                .authenticateWithMap(data, new CompletionListenerWithDataAndError<Object, BError>() {
                    @Override
                    public void onDone(Object authData) {
                        // Indexing the user.
                        getNetworkAdapter().updateIndexForUser(getNetworkAdapter().currentUser(), null);
                        afterLogin();
                    }

                    @Override
                    public void onDoneWithError(Object authData, BError o) {
                        toastErrorMessage(o, false);
                        dismissProgDialog();
                    }
                });
    }

    public void anonymosLogin(){
        showProgDialog(getString(R.string.connecting));

        Map<String, Object> data = new HashMap<String, Object>();
        data.put(BDefines.Prefs.LoginTypeKey, BDefines.BAccountType.Anonymous);

        BNetworkManager.sharedManager().getNetworkAdapter()
                .authenticateWithMap(data, new CompletionListenerWithDataAndError<Object, BError>() {
                    @Override
                    public void onDone(Object authData) {
                        afterLogin();
                    }

                    @Override
                    public void onDoneWithError(Object authData, BError o) {
                        toastErrorMessage(o, false);
                        dismissProgDialog();
                    }
                });
    }

    public void twitterLogin(){
        final DialogUtils.ChatSDKTwitterLoginDialog dialog = DialogUtils.ChatSDKTwitterLoginDialog.getInstance();
        dialog.setListener(new CompletionListenerWithDataAndError<Object, BError>(){
            @Override
            public void onDone(Object authData) {
                dialog.dismiss();
                showProgDialog(getString(R.string.authenticating));
                getNetworkAdapter().updateIndexForUser(getNetworkAdapter().currentUser(), null);
                afterLogin();
            }

            @Override
            public void onDoneWithError(Object authData, BError error) {
                dialog.dismiss();
                toastErrorMessage(error, true);
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
                if (DEBUG) Log.d(TAG, "Canceled");
                return;
            }
        }else showOrUpdateProgDialog(getString(R.string.authenticating));

        BFacebookManager.onSessionStateChange(session, state, exception, new CompletionListener() {
            @Override
            public void onDone() {
                if (DEBUG) Log.i(TAG, "FB is connected");
                getNetworkAdapter().updateIndexForUser(getNetworkAdapter().currentUser(), null);
                afterLogin();
            }

            @Override
            public void onDoneWithError(BError error) {
                if (DEBUG) Log.e(TAG, "Error connecting to FB or firebase");
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
