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
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 */
public class AbstractLoginActivity extends ChatSDKBaseActivity {

    private static final String TAG = AbstractLoginActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.LoginActivity;

    private boolean exitOnBackPressed = false;

    protected EditText etEmail, etPass;
    protected LoginButton facebookLogin;

    /** Passed to the activity in the intent extras, Indicates that the activity was called after the user press the logout button,
     * That means the activity wont try to authenticate in inResume. */
    public static final String FLAG_LOGGED_OUT = "LoggedOut";

    protected void initViews(){
        if (integratedWithFacebook)
            facebookLogin = (LoginButton) findViewById(R.id.chat_sdk_facebook_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume, Session State: " + Session.getActiveSession().getState().name());

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
                                    showAlertToast(getString(R.string.auth_failed));
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

        Map<String, Object> data = FirebasePaths.getMap(
                new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey},
                BDefines.BAccountType.Password, etEmail.getText().toString(), etPass.getText().toString());

        BNetworkManager.sharedManager().getNetworkAdapter()
                .authenticateWithMap(data, new CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object>() {
            @Override
            public void onDone(FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                afterLogin();
            }

            @Override
            public void onDoneWithError(FirebaseSimpleLoginUser firebaseSimpleLoginUser, Object o) {
                toastErrorMessage(o, true);

                dismissProgDialog();
            }
        });
    }

    public void register(){
        if (!checkFields())
            return;
        showProgDialog(getString(R.string.registering));

        Map<String, Object> data = FirebasePaths.getMap(
                new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey },
                BDefines.BAccountType.Register, etEmail.getText().toString(), etPass.getText().toString());

        BNetworkManager.sharedManager().getNetworkAdapter()
                .authenticateWithMap(data, new CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object>() {
            @Override
            public void onDone(FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                afterLogin();
            }

            @Override
            public void onDoneWithError(FirebaseSimpleLoginUser firebaseSimpleLoginUser, Object o) {
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
                .authenticateWithMap(data, new CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object>() {
            @Override
            public void onDone(FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                afterLogin();
            }

            @Override
            public void onDoneWithError(FirebaseSimpleLoginUser firebaseSimpleLoginUser, Object o) {
                toastErrorMessage(o, false);
                dismissProgDialog();
            }
        });
    }

    public void twitterLogin(){
        final DialogUtils.ChatSDKTwitterLoginDialog dialog = DialogUtils.ChatSDKTwitterLoginDialog.getInstance();
        dialog.setListener(new CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object>(){
            @Override
            public void onDone(FirebaseSimpleLoginUser user) {
                dialog.dismiss();
                showProgDialog(getString(R.string.authenticating));
                afterLogin();
            }

            @Override
            public void onDoneWithError(FirebaseSimpleLoginUser user, Object error) {
                dialog.dismiss();
                toastErrorMessage(error, true);
            }
        });
        dialog.show(getSupportFragmentManager(), "TwitterLogin");
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

    public void toastErrorMessage(Object o, boolean login){
        if (o instanceof FirebaseSimpleLoginError)
        {
            String toastMessage = "";
            FirebaseSimpleLoginError error = ((FirebaseSimpleLoginError) o);
            switch (error.getCode())
            {
                case EmailTaken:
                    toastMessage = "Email is taken.";
                    break;

                case InvalidEmail:
                    toastMessage = "Invalid Email.";
                    break;

                case InvalidPassword:
                    toastMessage = "Invalid Password";
                    break;

                case AccountNotFound:
                    toastMessage = "Account not found.";
                    break;

                case AccessNotGranted:
                    toastMessage = "Access not granted.";
                    break;

                case OperationFailed:
                    toastMessage = "Operation Failed";
                    break;

                case UserDoesNotExist:
                    toastMessage = "User does not exist";
                    break;

                case PermissionDenied:
                    toastMessage = "Permission denied";
                    break;

                default: toastMessage = "An Error Occurred.";
            }
            showAlertToast(toastMessage);
        }
        else if (login)
        {
            showAlertToast("Failed connect to Firebase.");
        }
        else {
            showAlertToast("Failed registering to Firebase.");
        }
    }

    protected boolean checkFields(){
        if (etEmail.getText().toString().isEmpty())
        {

//            Toast.makeText(LoginActivity.this, "Please enter username/email." , Toast.LENGTH_SHORT).show();
            showAlertToast("Please enter email.");
            return false;
        }

        if (etPass.getText().toString().isEmpty())
        {
//            Toast.makeText(LoginActivity.this, "Please enter password." , Toast.LENGTH_SHORT).show();
            showAlertToast( "Please enter password.");
            return false;
        }

        return true;
    }

    public void onSessionStateChange(Session session, SessionState state, Exception exception){
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
                if (DEBUG) Log.i(TAG, "FB and Firebase are connected");
                afterLogin();
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "Error connecting to FB or firebase");
//                Toast.makeText(LoginActivity.this, "Failed connect to facebook.", Toast.LENGTH_SHORT).show();
                showAlertToast("Failed connect to facebook.");
                dismissProgDialog();
            }
        });
    }

    public void setExitOnBackPressed(boolean exitOnBackPressed) {
        this.exitOnBackPressed = exitOnBackPressed;
    }
}
