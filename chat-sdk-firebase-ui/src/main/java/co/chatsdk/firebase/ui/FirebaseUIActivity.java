package co.chatsdk.firebase.ui;

import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;

import chatsdk.co.chat_sdk_firebase_ui.R;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.main.BaseActivity;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.utils.AppBackgroundMonitor;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            FirebaseUIModule.shared().impl_startAuthenticationActivity(FirebaseUIActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        authenticateWithCachedToken();
    }

    protected void authenticateWithCachedToken () {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            showProgressDialog(getString(R.string.authenticating));
            NM.auth().authenticateWithCachedToken()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally(() -> dismissProgressDialog())
                    .subscribe(() -> {
                        AppBackgroundMonitor.shared().setEnabled(true);
                        InterfaceManager.shared().a.startMainActivity(FirebaseUIActivity.this);
                    }, throwable -> throwable.printStackTrace());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == FirebaseUIModule.RC_SIGN_IN) {
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

                if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                    ToastHelper.show(this, R.string.no_internet_connection);
                    return;
                }

                if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    ToastHelper.show(this, R.string.unknown_error);
                    return;
                }
            }
            ToastHelper.show(this, R.string.unknown_sign_in_response);
//            finish();
        }
    }

}
