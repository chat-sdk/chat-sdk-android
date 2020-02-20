/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import org.pmw.tinylog.Logger;

import butterknife.BindView;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.databinding.ActivityLoginBinding;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;


/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    protected boolean exitOnBackPressed = false;
    protected boolean authenticating = false;

    ActivityLoginBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, getLayout());
        initViews();

        setExitOnBackPressed(true);

        setupTouchUIToDismissKeyboard(b.rootView);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }

    protected @LayoutRes int getLayout() {
        return R.layout.activity_login;
    }

    protected void initViews() {
        super.initViews();

        b.resetPasswordButton.setVisibility(ChatSDK.config().resetPasswordEnabled ? View.VISIBLE : View.INVISIBLE);

        if(!ChatSDK.auth().accountTypeEnabled(AccountDetails.Type.Anonymous)) {
            ((ViewGroup) b.anonymousButton.getParent()).removeView(b.anonymousButton);
        }

        // Set the debug username and password details for testing
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().debugUsername)) {
            b.usernameTextInput.setText(ChatSDK.config().debugUsername);
        }
        if(!StringChecker.isNullOrEmpty(ChatSDK.config().debugPassword)) {
            b.passwordTextInput.setText(ChatSDK.config().debugPassword);
        }

        b.appIconImageView.setImageResource(ChatSDK.config().logoDrawableResourceID);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initListeners() {

        b.loginButton.setOnClickListener(this);
        b.registerButton.setOnClickListener(this);
        b.anonymousButton.setOnClickListener(this);
        b.resetPasswordButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        showProgressDialog(getString(R.string.authenticating));

        getProgressDialog().setOnDismissListener(dialog -> {
            // Dispose
            dm.dispose();
        });

        if (i == R.id.loginButton) {
            passwordLogin();
        }
        else if (i == R.id.anonymousButton) {
            anonymousLogin();
        }
        else if (i == R.id.registerButton) {
            register();
        }
        else if (i == R.id.resetPasswordButton) {
            showForgotPasswordDialog();
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
        // we can load up the thread the text belongs to
//        ChatSDK.ui().startMainActivity(this, extras);
        finish();
    }

    public void passwordLogin() {
        if (!checkFields()) {
            dismissProgressDialog();
            return;
        }

        if(!isNetworkAvailable()) {
            Logger.debug("Network Connection unavailable");
        }

        AccountDetails details = AccountDetails.username(b.usernameTextInput.getText().toString(), b.passwordTextInput.getText().toString());

        authenticateWithDetails(details);
    }

    public void authenticateWithDetails (AccountDetails details) {

        if(authenticating) {
            return;
        }
        authenticating = true;

        showProgressDialog(getString(R.string.connecting));

        dm.add(ChatSDK.auth().authenticate(details)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    authenticating = false;
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
        details.username = b.usernameTextInput.getText().toString();
        details.password = b.passwordTextInput.getText().toString();

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

        if (!error.getMessage().replace(" ", "").isEmpty()) {
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
        if (b.usernameTextInput.getText().toString().isEmpty()) {
            showToast(getString(R.string.login_activity_no_mail_toast));
            return false;
        }

        if (b.passwordTextInput.getText().toString().isEmpty()) {
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
            dm.add(requestNewPassword(input.getText().toString()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
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
