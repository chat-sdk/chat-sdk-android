package co.chatsdk.firebase.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import chatsdk.co.chat_sdk_firebase_ui.R;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.utils.AppBackgroundMonitor;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static co.chatsdk.firebase.ui.FirebaseUIModule.RC_SIGN_IN;

/**
 * Created by ben on 1/2/18.
 */

public class SplashScreenActivity extends BaseActivity {

    Button signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_firebase_ui_activity);

        signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAuthenticationActivity();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        authenticateWithCachedToken();
    }

    public void startAuthenticationActivity () {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(FirebaseUIModule.shared().getIdps())
                        .build(),
                RC_SIGN_IN);
    }

    protected void authenticateWithCachedToken () {
        showProgressDialog(getString(R.string.authenticating));
        signInButton.setEnabled(false);
        NM.auth().authenticateWithCachedToken()
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    signInButton.setEnabled(true);
                    dismissProgressDialog();
                })
                .subscribe(() -> {
                    AppBackgroundMonitor.shared().setEnabled(true);
                    InterfaceManager.shared().a.startMainActivity(SplashScreenActivity.this);
                }, throwable -> {
//                    startAuthenticationActivity();
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {
                return;
            }
            else {

                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    ToastHelper.show(this, R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    ToastHelper.show(this, R.string.no_internet_connection);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    ToastHelper.show(this, R.string.unknown_error);
                    return;
                }
            }
            ToastHelper.show(this, R.string.unknown_sign_in_response);
//            finish();
        }
    }

}
