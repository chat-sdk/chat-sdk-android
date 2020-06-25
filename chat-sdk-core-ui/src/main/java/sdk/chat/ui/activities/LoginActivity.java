/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.pmw.tinylog.Logger;

import butterknife.BindView;
import io.reactivex.Completable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.AccountDetails;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.RX;


/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {

    protected boolean exitOnBackPressed = false;
    protected boolean authenticating = false;

    @BindView(R2.id.appIconImageView) protected ImageView appIconImageView;
    @BindView(R2.id.usernameTextInput) protected TextInputEditText usernameTextInput;
    @BindView(R2.id.usernameTextInputLayout) protected TextInputLayout usernameTextInputLayout;
    @BindView(R2.id.passwordTextInput) protected TextInputEditText passwordTextInput;
    @BindView(R2.id.passwordTextInputLayout) protected TextInputLayout passwordTextInputLayout;
    @BindView(R2.id.loginButton) protected MaterialButton loginButton;
    @BindView(R2.id.registerButton) protected MaterialButton registerButton;
    @BindView(R2.id.anonymousButton) protected MaterialButton anonymousButton;
    @BindView(R2.id.resetPasswordButton) protected MaterialButton resetPasswordButton;
    @BindView(R2.id.root) protected ConstraintLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViews();

        setExitOnBackPressed(true);

        setupTouchUIToDismissKeyboard(root);

        initViews();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }

    protected void initViews() {
        super.initViews();

        resetPasswordButton.setVisibility(UIModule.config().resetPasswordEnabled ? View.VISIBLE : View.INVISIBLE);

        if (!ChatSDK.auth().accountTypeEnabled(AccountDetails.Type.Anonymous)) {
            anonymousButton.setVisibility(View.GONE);
        }

        // Set the debug username and password details for testing
        if (!StringChecker.isNullOrEmpty(ChatSDK.config().debugUsername)) {
            usernameTextInput.setText(ChatSDK.config().debugUsername);
        }
        if (!StringChecker.isNullOrEmpty(ChatSDK.config().debugPassword)) {
            passwordTextInput.setText(ChatSDK.config().debugPassword);
        }

        if (UIModule.config().usernameHint != null) {
            usernameTextInputLayout.setHint(UIModule.config().usernameHint);
        }

        appIconImageView.setImageResource(ChatSDK.config().logoDrawableResourceID);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void initListeners() {

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
        anonymousButton.setOnClickListener(this);
        resetPasswordButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        v.setEnabled(false);

        int i = v.getId();

        showProgressDialog(getString(R.string.authenticating));

        getProgressDialog().setOnDismissListener(dialog -> {
            v.setEnabled(true);
            dm.dispose();
            ChatSDK.auth().cancel();
        });

        if (i == R.id.loginButton) {
            passwordLogin();
        } else if (i == R.id.anonymousButton) {
            anonymousLogin();
        } else if (i == R.id.registerButton) {
            register();
        } else if (i == R.id.resetPasswordButton) {
            showForgotPasswordDialog();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loginButton.setEnabled(true);
        registerButton.setEnabled(true);
        anonymousButton.setEnabled(true);
        resetPasswordButton.setEnabled(true);

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

        if (!isNetworkAvailable()) {
            Logger.debug("Network Connection unavailable");
        }

        AccountDetails details = AccountDetails.username(usernameTextInput.getText().toString(), passwordTextInput.getText().toString());

        authenticateWithDetails(details);
    }

    public void authenticateWithDetails(AccountDetails details) {

        if (authenticating) {
            return;
        }
        authenticating = true;

        showProgressDialog(getString(R.string.connecting));

        dm.add(ChatSDK.auth().authenticate(details)
                .observeOn(RX.main())
                .doFinally(() -> {
                    authenticating = false;
                })
                .subscribe(this::afterLogin, e -> {
                    dismissProgressDialog();
                    toastErrorMessage(e, false);
                    ChatSDK.events().onError(e);
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
        details.username = usernameTextInput.getText().toString();
        details.password = passwordTextInput.getText().toString();

        authenticateWithDetails(details);

    }

    public void anonymousLogin() {

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
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else super.onBackPressed();

    }

    public void toastErrorMessage(Throwable error, boolean login) {
        String errorMessage = "";

        if (!error.getMessage().replace(" ", "").isEmpty()) {
            errorMessage = error.getMessage();
        } else if (login) {
            errorMessage = getString(R.string.login_activity_failed_to_login_toast);
        } else {
            errorMessage = getString(R.string.login_activity_failed_to_register_toast);
        }

        showToast(errorMessage);
    }

    protected boolean checkFields() {
        if (usernameTextInput.getText().toString().isEmpty()) {
            showToast(getString(R.string.login_activity_no_mail_toast));
            return false;
        }

        if (passwordTextInput.getText().toString().isEmpty()) {
            showToast(getString(R.string.login_activity_no_password_toast));
            return false;
        }

        return true;
    }

    protected void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.forgot_password));

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton(getString(R.string.submit), (dialog, which) -> {
            showOrUpdateProgressDialog(getString(R.string.requesting));
            dm.add(requestNewPassword(input.getText().toString()).observeOn(RX.main()).subscribe(() -> {
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

    protected Completable requestNewPassword(String email) {
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
