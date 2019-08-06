/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;

import androidx.constraintlayout.widget.ConstraintLayout;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseActivity;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    protected boolean exitOnBackPressed = false;
    protected ConstraintLayout mainView;
    protected boolean authenticating = false;

    protected TextInputEditText usernameEditText;
    protected TextInputEditText passwordEditText;

    /** Passed to the context in the intent extras, Indicates that the context was called after the user press the logout button,
     * That means the context wont try to authenticate in inResume. */

    protected MaterialButton btnLogin, btnReg,  btnAnonymous, btnResetPassword;
    protected ImageButton btnTwitter, btnGoogle, btnFacebook;
    protected ImageView appIconImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setExitOnBackPressed(true);

        setContentView(activityLayout());

        mainView = findViewById(R.id.view_root);
        setupTouchUIToDismissKeyboard(mainView);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }

    protected @LayoutRes int activityLayout() {
        return R.layout.activity_login;
    }

    protected void initViews() {
        btnLogin = findViewById(R.id.button_login);
        btnAnonymous = findViewById(R.id.button_anonymous_login);
        btnTwitter = findViewById(R.id.button_twitter);
        btnReg = findViewById(R.id.button_register);
        usernameEditText = findViewById(R.id.text_input_username);
        passwordEditText = findViewById(R.id.text_input_password);
        btnGoogle = findViewById(R.id.button_google);
        btnFacebook = findViewById(R.id.button_facebook);
        appIconImage = findViewById(R.id.image_app_icon);
        btnResetPassword = findViewById(R.id.button_reset_password);

        btnResetPassword.setVisibility(ChatSDK.config().resetPasswordEnabled ? View.VISIBLE : View.INVISIBLE);

        if(!ChatSDK.auth().accountTypeEnabled(AccountDetails.Type.Facebook)) {
            ((ViewGroup) btnFacebook.getParent()).removeView(btnFacebook);
        }
        if(!ChatSDK.auth().accountTypeEnabled(AccountDetails.Type.Twitter)) {
            ((ViewGroup) btnTwitter.getParent()).removeView(btnTwitter);
        }
        if(!ChatSDK.auth().accountTypeEnabled(AccountDetails.Type.Google)) {
            ((ViewGroup) btnGoogle.getParent()).removeView(btnGoogle);
        }
        if(!ChatSDK.auth().accountTypeEnabled(AccountDetails.Type.Anonymous)) {
            ((ViewGroup) btnAnonymous.getParent()).removeView(btnAnonymous);
        }

        // Set the debug username and password details for testing
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().debugUsername)) {
            usernameEditText.setText(ChatSDK.config().debugUsername);
        }
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().debugPassword)) {
            passwordEditText.setText(ChatSDK.config().debugPassword);
        }

        appIconImage.setImageResource(ChatSDK.config().logoDrawableResourceID);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(ChatSDK.socialLogin() != null) {
            ChatSDK.socialLogin().onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void initListeners() {

        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnAnonymous.setOnClickListener(this);
        btnTwitter.setOnClickListener(this);
        btnFacebook.setOnClickListener(this);
        btnGoogle.setOnClickListener(this);
        btnResetPassword.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        Action completion = this::afterLogin;

        Consumer<Throwable> error = throwable -> {
            ChatSDK.logError(throwable);
            Toast.makeText(LoginActivity.this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        };

        Action doFinally = this::dismissProgressDialog;

        showProgressDialog(getString(R.string.authenticating));
        progressDialog.setOnDismissListener(dialog -> {
            // Dispose
            disposableList.dispose();
        });

        if (i == R.id.button_login) {
            passwordLogin();
        }
        else if (i == R.id.button_anonymous_login) {
            anonymousLogin();
        }
        else if (i == R.id.button_register) {
            register();
        }
        else if (i == R.id.button_reset_password) {
            showForgotPasswordDialog();
        }
        else if (i == R.id.button_twitter) {
            if(ChatSDK.socialLogin() != null) {
                disposableList.add(ChatSDK.socialLogin().loginWithTwitter(this).doOnError(error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(doFinally)
                        .subscribe(completion, error));
            }
        }
        else if (i == R.id.button_facebook) {
            if(ChatSDK.socialLogin() != null) {
                disposableList.add(ChatSDK.socialLogin().loginWithFacebook(this).doOnError(error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(doFinally)
                        .subscribe(completion, error));
            }
        }
        else if (i == R.id.button_google) {
            if(ChatSDK.socialLogin() != null) {
                disposableList.add(ChatSDK.socialLogin().loginWithGoogle(this).doOnError(error)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(doFinally)
                        .subscribe(completion, error));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initListeners();
    }

    /* Dismiss dialog and open main context.*/
    protected void afterLogin() {
        // We pass the extras in case this activity was launched by a push. In that case
        // we can load up the thread the message belongs to
//        ChatSDK.ui().startMainActivity(this, extras);
        finish();
    }

    public void passwordLogin() {
        if (!checkFields()) {
            dismissProgressDialog();
            return;
        }

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

        disposableList.add(ChatSDK.auth().authenticate(details)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    authenticating = false;
//                    dismissProgressDialog();
                })
                .subscribe(this::afterLogin, e -> {
                    dismissProgressDialog();
                    toastErrorMessage(e, false);
                    ChatSDK.logError(e);
                }));
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
    }

    public void register() {

        if (!checkFields()) {
            dismissProgressDialog();
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

    protected void showForgotPasswordDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.forgot_password));

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.submit), (dialog, which) -> {
            showOrUpdateProgressDialog(getString(R.string.requesting));
            disposableList.add(requestNewPassword(input.getText().toString()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
                dismissProgressDialog();
                showToast(getString(R.string.password_reset_success));
            }, throwable -> {
                showToast(throwable.getLocalizedMessage());
            }));
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
            dialog.cancel();
            dismissProgressDialog();
        });

        builder.show();

    }

    protected Completable requestNewPassword (String email) {
        return ChatSDK.auth().sendPasswordResetMail(email);
    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected void setExitOnBackPressed(boolean exitOnBackPressed) {
        this.exitOnBackPressed = exitOnBackPressed;
    }


}
