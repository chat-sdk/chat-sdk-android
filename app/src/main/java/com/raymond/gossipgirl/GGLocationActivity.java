package com.raymond.gossipgirl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseUiException;
import com.firebase.ui.auth.IdpResponse;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.ui.utils.ToastHelper;

import static co.chatsdk.firebase.ui.FirebaseUIModule.RC_SIGN_IN;

public class GGLocationActivity extends AppCompatActivity {

    private DisposableList disposableList = new DisposableList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    public void didClickOnContinue(View v) {
        startAuthenticationActivity();
    }

    public void startAuthenticationActivity() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(FirebaseUIModule.shared().getIdps())
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                disposableList.add(ChatSDK.auth().authenticateWithCachedToken().doFinally(() -> {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }).subscribe(() -> {
                    String city = ChatSDK.currentUser().metaStringForKey("city");
                    if (city == null) {
                        ChatSDK.ui().startActivity(getApplicationContext(), GGUsernameActivity.class);
                    }
                    else {
                        ChatSDK.ui().startMainActivity(getApplicationContext());
                    }
                }, Throwable::printStackTrace));

                return;
            }
            else { // Sign in failed
                if (response == null) { // User pressed back button
                    ToastHelper.show(getApplicationContext(), R.string.sign_in_cancelled);
                    return;
                }

                FirebaseUiException error = response.getError();

                if (error != null && error.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    ToastHelper.show(getApplicationContext(), R.string.no_internet_connection);
                    return;
                }

                if (error != null && error.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    ToastHelper.show(getApplicationContext(), R.string.unknown_error);
                    return;
                }
            }

            ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.unknown_sign_in_response);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposableList.dispose();
    }

}
