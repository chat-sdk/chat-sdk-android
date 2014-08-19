package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.firebase.simplelogin.User;
import com.firebase.simplelogin.enums.Error;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.braunster.chatsdk.Utils.DialogUtils.ChatSDKTwitterLoginDialog;

/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static boolean DEBUG = Debug.LoginActivity;

    /** Passed to the activity in the intent extras, Indicates that the activity was called after the user press the logout button,
     * That means the activity wont try to authenticate in inResume. */
    public static final String FLAG_LOGGED_OUT = "LoggedOut";

    private UiLifecycleHelper uiHelper;
    private LoginButton fbLoginButton;
    private Button btnLogin, btnReg, btnAnon, btnTwitter;
    private EditText etName, etPass;
    private ImageView appIconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activty_login);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        View view = findViewById(R.id.chat_sdk_root_view);
        if (DEBUG) Log.d(TAG, "View is: " + (view == null ? "null" : "not null"));
        setupTouchUIToDismissKeyboard(view);

        initViews();
        initToast();

/*
        // For registering the activity to facebook.
        String sha = Utils.getSHA(this, getPackageName());
        Log.i(TAG, "SHA: " + sha);*/
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if(DEBUG) Log.v(TAG, "onNewIntent");
    }

    private void initViews(){
        fbLoginButton = (LoginButton) findViewById(R.id.chat_sdk_facebook_button);
        fbLoginButton.setOnErrorListener(new LoginButton.OnErrorListener() {
            @Override
            public void onError(FacebookException error) {
                showAlertToast("Fb Login button error");
            }
        });
        fbLoginButton.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        fbLoginButton.setBackgroundResource(R.drawable.ic_facebook);
        fbLoginButton.setReadPermissions(Arrays.asList("email", "user_friends"));

        btnLogin = (Button) findViewById(R.id.chat_sdk_btn_login);
        btnAnon = (Button) findViewById(R.id.chat_sdk_btn_anon_login);
        btnTwitter = (Button) findViewById(R.id.chat_sdk_btn_twitter_login);
        btnReg = (Button) findViewById(R.id.chat_sdk_btn_register);
        etName = (EditText) findViewById(R.id.chat_sdk_et_mail);
        etPass = (EditText) findViewById(R.id.chat_sdk_et_password);

        appIconImage = (ImageView) findViewById(R.id.app_icon);

        appIconImage.post(new Runnable() {
            @Override
            public void run() {
                appIconImage.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initListeners(){
        /* Registering listeners.*/
        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnAnon.setOnClickListener(this);
        fbLoginButton.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);

        etPass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    btnLogin.callOnClick();
                }
                return false;
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (DEBUG) Log.d(TAG, "onResume, Session State: " + Session.getActiveSession().getState().name());

        uiHelper.onResume();

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
                                    showToast(getString(R.string.auth_failed));
                            }
                        }
                );
            }

        initListeners();
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

    /* Dismiss dialog and open main activity.*/
    private void afterLogin(){
//        // Giving the system extra time to load.
        findViewById(R.id.chat_sdk_root_view).postDelayed(new Runnable() {
            @Override
            public void run() {
                dismissProgDialog();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(BaseActivity.FROM_LOGIN, true);
                startActivity(intent);
            }
        }, 5000);
    }

    /* Exit Stuff*/
    @Override
    public void onBackPressed() {
        // Exit the app.
        // If logged out from the main activity pressing back in the LoginActivity will get me back to the Main so this have to be done.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.chat_sdk_btn_login) {

            if (!checkFields())
                return;

            showProgDialog(getString(R.string.connecting));

            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey },
                    BDefines.BAccountType.Password, etName.getText().toString(), etPass.getText().toString());
            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(data, new CompletionListenerWithDataAndError<User, Object>() {
                @Override
                public void onDone(User firebaseSimpleLoginUser) {
                    afterLogin();
                }

                @Override
                public void onDoneWithError(User firebaseSimpleLoginUser, Object o) {
                   toastErrorMessage(o, true);

                    dismissProgDialog();
                }
            });
        }
        else if (i == R.id.chat_sdk_btn_anon_login) {
            showProgDialog(getString(R.string.connecting));

            Map<String, Object> data = new HashMap<String, Object>();
            data.put(BDefines.Prefs.LoginTypeKey, BDefines.BAccountType.Anonymous);
            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(data, new CompletionListenerWithDataAndError<User, Object>() {
                @Override
                public void onDone(User firebaseSimpleLoginUser) {
                    afterLogin();
                }

                @Override
                public void onDoneWithError(User firebaseSimpleLoginUser, Object o) {
                    toastErrorMessage(o, false);
                    dismissProgDialog();
                }
            });
        }
        else if (i == R.id.chat_sdk_btn_register)
        {
            if (!checkFields())
                return;
            showProgDialog(getString(R.string.registering));

            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey },
                    BDefines.BAccountType.Register, etName.getText().toString(), etPass.getText().toString());
            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(data, new CompletionListenerWithDataAndError<User, Object>() {
                @Override
                public void onDone(User firebaseSimpleLoginUser) {
                    afterLogin();
                }

                @Override
                public void onDoneWithError(User firebaseSimpleLoginUser, Object o) {
                    toastErrorMessage(o, false);
                    dismissProgDialog();
                }
            });
        }
        else if (i == R.id.chat_sdk_btn_twitter_login){
            final ChatSDKTwitterLoginDialog dialog = ChatSDKTwitterLoginDialog.getInstance();
            dialog.setListener(new CompletionListenerWithDataAndError<User, Object>(){
                @Override
                public void onDone(User user) {
                    dialog.dismiss();
                    showProgDialog(getString(R.string.authenticating));
                    afterLogin();
                }

                @Override
                public void onDoneWithError(User user, Object error) {
                    dialog.dismiss();
                    toastErrorMessage(error, true);
                }
            });
            dialog.show(getSupportFragmentManager(), "TwitterLogin");
        }
    }

    private void toastErrorMessage(Object o, boolean login){
        if (o instanceof Error)
        {
            String toastMessage = "";
            Error error = ((Error) o);
            switch (error)
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
                    toastMessage = "Premission denied";
                    break;

                default: toastMessage = "An Error Occurred.";
            }
            showToast(toastMessage);
//            Toast.makeText(LoginActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
        }
        else if (login)
        {
//            Toast.makeText(LoginActivity.this, "Failed connect to Firebase.", Toast.LENGTH_SHORT).show();
            showToast("Failed connect to Firebase.");
        }
        else {
//            Toast.makeText(LoginActivity.this, "Failed registering to Firebase.", Toast.LENGTH_SHORT).show();
            showToast( "Failed registering to Firebase.");
        }
    }

    private boolean checkFields(){
        if (etName.getText().toString().isEmpty())
        {

//            Toast.makeText(LoginActivity.this, "Please enter username/email." , Toast.LENGTH_SHORT).show();
            showToast("Please enter username/email.");
            return false;
        }

        if (etPass.getText().toString().isEmpty())
        {
//            Toast.makeText(LoginActivity.this, "Please enter password." , Toast.LENGTH_SHORT).show();
            showToast( "Please enter password.");
            return false;
        }

        return true;
    }

    // Facebook Login stuff.
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        showOrUpdateProgDialog(getString(R.string.authenticating));
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
                showToast("Failed connect to facebook.");
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
