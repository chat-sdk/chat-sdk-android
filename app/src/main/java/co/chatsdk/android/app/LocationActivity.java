package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.ui.FirebaseUIModule;
import co.chatsdk.firebase.ui.SplashScreenActivity;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static co.chatsdk.firebase.ui.FirebaseUIModule.RC_SIGN_IN;

public class LocationActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    public void continue_click(View v) {
        startAuthenticationActivity();
    }

    public void startAuthenticationActivity () {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(FirebaseUIModule.shared().getIdps())
                        .build(),
                RC_SIGN_IN);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {

            IdpResponse response = IdpResponse.fromResultIntent(data);
/*
                    FirebaseAuth.getInstance().signInWithCredential(response.getCredentialForLinking()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            authResult.getAdditionalUserInfo().isNewUser();
                            System.out.print("x");
                        }
                    });

*/

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                ChatSDK.auth().authenticateWithCachedToken().doFinally(() -> {
                    findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }).subscribe(() -> {
                    String location = ChatSDK.currentUser().metaStringForKey("city");
                    if (location == null) {
                        Intent i = new Intent (LocationActivity.this, GossipGirlUsernameActivity.class);
                        startActivity(i);
                    }
                    else {
                        ChatSDK.ui().startMainActivity(LocationActivity.this);
                    }
                }, throwable -> throwable.printStackTrace());

                return;
            }
            else {

                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.sign_in_cancelled);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.no_internet_connection);
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.unknown_error);
                    return;
                }
            }
            ToastHelper.show(this, chatsdk.co.chat_sdk_firebase_ui.R.string.unknown_sign_in_response);
//            finish();
        }
    }
}
