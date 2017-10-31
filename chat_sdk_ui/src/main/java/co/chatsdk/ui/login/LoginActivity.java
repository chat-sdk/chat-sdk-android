/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.login;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.utils.AppBackgroundMonitor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private boolean exitOnBackPressed = false;
    private LinearLayout mainView;
    private boolean authenticating = false;

    protected EditText usernameEditText;
    protected EditText passwordEditText;

    /** Passed to the context in the intent extras, Indicates that the context was called after the user press the logout button,
     * That means the context wont try to authenticate in inResume. */

    private Button btnLogin, btnReg, btnAnonymous, btnTwitter, btnGoogle, btnFacebook;
    private ImageView appIconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chat_sdk_activty_login);

        setExitOnBackPressed(true);

        mainView = (LinearLayout) findViewById(R.id.chat_sdk_root_view);

        setupTouchUIToDismissKeyboard(mainView);

        getSupportActionBar().hide();

        initViews();

        PermissionRequestHandler.shared().requestReadExternalStorage(this).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                throwable.printStackTrace();
            }
        }).subscribe();

    }

    protected void initViews () {

        btnLogin = (Button) findViewById(R.id.chat_sdk_btn_login);
        btnAnonymous = (Button) findViewById(R.id.chat_sdk_btn_anon_login);
        btnTwitter = (Button) findViewById(R.id.chat_sdk_btn_twitter_login);
        btnReg = (Button) findViewById(R.id.chat_sdk_btn_register);
        usernameEditText = (EditText) findViewById(R.id.chat_sdk_et_username);
        passwordEditText = (EditText) findViewById(R.id.chat_sdk_et_password);
        btnGoogle = (Button) findViewById(R.id.chat_sdk_btn_google_login);
        btnFacebook = (Button) findViewById(R.id.chat_sdk_btn_facebook_login);
        appIconImage = (ImageView) findViewById(R.id.app_icon);

        if(!NM.auth().accountTypeEnabled(AccountDetails.Type.Facebook)) {
            ((ViewGroup) btnFacebook.getParent()).removeView(btnFacebook);
        }
        if(!NM.auth().accountTypeEnabled(AccountDetails.Type.Twitter)) {
            ((ViewGroup) btnTwitter.getParent()).removeView(btnTwitter);
        }
        if(!NM.auth().accountTypeEnabled(AccountDetails.Type.Google)) {
            ((ViewGroup) btnGoogle.getParent()).removeView(btnGoogle);
        }
        if(!NM.auth().accountTypeEnabled(AccountDetails.Type.Anonymous)) {
            ((ViewGroup) btnAnonymous.getParent()).removeView(btnAnonymous);
        }

        // Set the debug username and password details for testing
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().debugUsername)) {
            usernameEditText.setText(ChatSDK.config().debugUsername);
        }
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().debugPassword)) {
            passwordEditText.setText(ChatSDK.config().debugPassword);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(NM.socialLogin() != null) {
            NM.socialLogin().onActivityResult(requestCode, resultCode, data);
        }
    }

    private void initListeners() {

        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnAnonymous.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
    public void onClick(View v) {
        int i = v.getId();

        Action completion = new Action() {
            @Override
            public void run() throws Exception {
                afterLogin();
            }
        };

        Consumer<Throwable> error = new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                throwable.printStackTrace();
                Toast.makeText(LoginActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        };

        Action doFinally = new Action() {
            @Override
            public void run() throws Exception {
                dismissProgressDialog();
            }
        };

        showProgressDialog(getString(R.string.authenticating));

        if (i == R.id.chat_sdk_btn_login) {
            passwordLogin();
        }
        else if (i == R.id.chat_sdk_btn_anon_login) {
            anonymousLogin();
        }
        else if (i == R.id.chat_sdk_btn_register) {
            register();
        }
        else if (i == R.id.chat_sdk_btn_twitter_login) {
            if(NM.socialLogin() != null) {
                NM.socialLogin().loginWithTwitter(this).doOnError(error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(doFinally)
                        .subscribe(completion, error);
            }
        }
        else if (i == R.id.chat_sdk_btn_facebook_login) {
            if(NM.socialLogin() != null) {
                NM.socialLogin().loginWithFacebook(this).doOnError(error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(doFinally)
                        .subscribe(completion, error);
            }
        }
        else if (i == R.id.chat_sdk_btn_google_login) {
            if(NM.socialLogin() != null) {
                NM.socialLogin().loginWithGoogle(this).doOnError(error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(doFinally)
                        .subscribe(completion, error);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        AppBackgroundMonitor.shared().setEnabled(false);

        initListeners();

        // If the logged out flag isn't set...
        if (getIntent() == null ||
                getIntent().getExtras() == null ||
                getIntent().getExtras().get(BaseInterfaceAdapter.ATTEMPT_CACHED_LOGIN) == null ||
                (boolean) getIntent().getExtras().get(BaseInterfaceAdapter.ATTEMPT_CACHED_LOGIN)) {

            showProgressDialog(getString(R.string.authenticating));

            NM.auth().authenticateWithCachedToken()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(new Action() {
                        @Override
                        public void run() throws Exception {
                            dismissProgressDialog();
                        }
                    })
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            afterLogin();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            dismissProgressDialog();
                            // This is annoying because if the login fails it just says - details not valid...
//                            Toast.makeText(LoginActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /* Dismiss dialog and open main context.*/
    protected void afterLogin() {
        AppBackgroundMonitor.shared().setEnabled(true);
        InterfaceManager.shared().a.startMainActivity(this);
    }

    public void passwordLogin() {
        if (!checkFields())
            return;

        if(!isNetworkAvailable()) {
            Timber.v("Network Connection unavailable");
        }

        AccountDetails details = AccountDetails.username(usernameEditText.getText().toString(), passwordEditText.getText().toString());

        authenticateWithDetails(details);
    }

    public void authenticateWithDetails (AccountDetails details) {

        if(authenticating) {
            return;
        }
        authenticating = true;

        showProgressDialog(getString(R.string.connecting));

        NM.auth().authenticate(details)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        authenticating = false;
                        dismissProgressDialog();
                    }
                })
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        afterLogin();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        toastErrorMessage(e, false);
                        e.printStackTrace();
                    }
                });
    }

    public void register() {

        if (!checkFields()) {
            return;
        }

        AccountDetails details = new AccountDetails();
        details.type = AccountDetails.Type.Register;
        details.username = usernameEditText.getText().toString();
        details.password = passwordEditText.getText().toString();

        authenticateWithDetails(details);

    }

    public void anonymousLogin () {

        AccountDetails details = new AccountDetails();
        details.type = AccountDetails.Type.Anonymous;
        authenticateWithDetails(details);
    }

    /* Exit Stuff*/
    @Override
    public void onBackPressed() {
        if (exitOnBackPressed) {
            // Exit the app.
            // If logged out from the main context pressing back in the LoginActivity will get me back to the Main so this have to be done.
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else super.onBackPressed();

    }

    public void toastErrorMessage(Throwable error, boolean login){
        String errorMessage = "";

        if (StringUtils.isNotBlank(error.getMessage())) {
            errorMessage = error.getMessage();
        }
        else if (login) {
            errorMessage = getString(R.string.login_activity_failed_to_login_toast);
        }
        else {
            errorMessage = getString(R.string.login_activity_failed_to_register_toast);
        }

        showToast(errorMessage);
    }

    protected boolean checkFields(){
        if (usernameEditText.getText().toString().isEmpty()) {
            showToast(getString(R.string.login_activity_no_mail_toast));
            return false;
        }

        if (passwordEditText.getText().toString().isEmpty()) {
            showToast( getString(R.string.login_activity_no_password_toast) );
            return false;
        }

        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void setExitOnBackPressed(boolean exitOnBackPressed) {
        this.exitOnBackPressed = exitOnBackPressed;
    }
}
