/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.login;

import android.content.Intent;
import android.widget.EditText;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.LoginType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.activities.BaseActivity;
import co.chatsdk.ui.activities.MainActivity;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.ui.helpers.DialogUtils;
import com.braunster.chatsdk.network.FacebookManager;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by itzik on 6/8/2014.
 */
public class AbstractLoginActivity extends BaseActivity {

    private static final String TAG = AbstractLoginActivity.class.getSimpleName();
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
        Map<String, ?> loginInfo = NM.auth().getLoginInfo();

        if (loginInfo != null && loginInfo.containsKey(Defines.Prefs.AccountTypeKey))
            if (getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey(FLAG_LOGGED_OUT)) {

                showProgDialog(getString(R.string.authenticating));

                authenticate().subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {}

                    @Override
                    public void onComplete() {
                        if (DEBUG) Timber.d("Authenticated");
                        dismissProgDialog();
                        afterLogin();
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgDialog();
                        if (DEBUG) Timber.d("Auth Failed");
                        //TODO: remove if not needed.
                        //if (chatError.code != ChatError.Code.NO_LOGIN_INFO)
                        //    showToast(getString(R.string.login_activity_auth_failed));

                    }
                });
            }
    }

    /* Dismiss dialog and open main activity.*/
    protected void afterLogin(){
        // Indexing the user.
        User currentUser = NM.currentUser();
        if(currentUser != null) {
            NM.core().pushUser().subscribe();
        }

        Intent logout = new Intent(MainActivity.Action_clear_data);
        sendBroadcast(logout);

        dismissProgDialog();
    }

    public void passwordLogin(){
        if (!checkFields())
            return;

        showProgDialog(getString(R.string.connecting));

        Map<String, Object> data = new HashMap<String, Object>();

        data.put(LoginType.TypeKey, AccountType.Password);
        data.put(LoginType.EmailKey, etEmail.getText().toString());
        data.put(LoginType.PasswordKey, etPass.getText().toString());

        NM.auth().authenticateWithMap(data).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                afterLogin();
            }

            @Override
            public void onError(Throwable e) {
                toastErrorMessage(e, false);
                e.printStackTrace();
                dismissProgDialog();
            }
        });
    }

    public void register(){
        if (!checkFields())
            return;

        showProgDialog(getString(R.string.registering));

        Map<String, Object> data = new HashMap<String, Object>();

        data.put(LoginType.TypeKey, AccountType.Register);
        data.put(LoginType.EmailKey, etEmail.getText().toString());
        data.put(LoginType.PasswordKey, etPass.getText().toString());

        NM.auth().authenticateWithMap(data).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {}

            @Override
            public void onComplete() {
                afterLogin();
            }

            @Override
            public void onError(Throwable e) {
                toastErrorMessage(e, false);
                dismissProgDialog();
            }
        });
    }

    public void anonymousLogin(){
        showProgDialog(getString(R.string.connecting));

        Map<String, Object> data = new HashMap<String, Object>();
        data.put(LoginType.TypeKey, AccountType.Anonymous);

        NM.auth().authenticateWithMap(data).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                afterLogin();
            }

            @Override
            public void onError(Throwable e) {
                toastErrorMessage(e, false);
                dismissProgDialog();
            }
        });

    }

    public void twitterLogin(){

        if (!NM.auth().accountTypeEnabled(AccountType.Twitter))
        {
            showToast("Twitter is disabled.");
            return;
        }

        final DialogUtils.ChatSDKTwitterLoginDialog dialog = DialogUtils.ChatSDKTwitterLoginDialog.getInstance();
        dialog.addCompletionHandler(new DialogUtils.ChatSDKTwitterLoginDialog.Completion() {
            @Override
            public void complete(Throwable e) {
                if(e == null) {
                    dialog.dismiss();
                    showProgDialog(getString(R.string.authenticating));
                    afterLogin();
                }
                else {
                    dialog.dismiss();
                    toastErrorMessage(e, true);
                }
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

                showToast("Facebook error: " + error.getMessage() + " " + error.getClass().getSimpleName());
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

    public void toastErrorMessage(Throwable error, boolean login){
        String errorMessage = "";

        if (StringUtils.isNotBlank(error.getMessage()))
            errorMessage = error.getMessage();
        else if (login)
            errorMessage = getString(R.string.login_activity_failed_to_login_toast);
        else
            errorMessage = getString(R.string.login_activity_failed_to_register_toast);


        showToast(errorMessage);
    }

    protected boolean checkFields(){
        if (etEmail.getText().toString().isEmpty())
        {
            showToast(getString(R.string.login_activity_no_mail_toast));
            return false;
        }

        if (etPass.getText().toString().isEmpty())
        {
            showToast( getString(R.string.login_activity_no_password_toast) );
            return false;
        }

        return true;
    }

    public void onSessionStateChange(Session session, SessionState state, Exception exception){

        if (!NM.auth().accountTypeEnabled(AccountType.Facebook))
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

        FacebookManager.onSessionStateChange(session, state, exception).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onComplete() {
                if (DEBUG) Timber.i("Connected to facebook");
                afterLogin();
            }

            @Override
            public void onError(Throwable e) {
                if (DEBUG) Timber.i(TAG, "Error connecting to Facebook");
                showToast( getString(R.string.login_activity_facebook_connection_fail_toast) );
                FacebookManager.logout(AbstractLoginActivity.this);
                dismissProgDialog();
            }
        });
    }

    protected void setExitOnBackPressed(boolean exitOnBackPressed) {
        this.exitOnBackPressed = exitOnBackPressed;
    }
}
