package co.chatsdk.firebase.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;

import java.util.ArrayList;

import co.chatsdk.ui.manager.InterfaceManager;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIModule {

    public static final int RC_SIGN_IN = 900;

    private ArrayList<AuthUI.IdpConfig> idps = new ArrayList<>();
    protected static final FirebaseUIModule instance = new FirebaseUIModule();

    public static void activate (Context context, String... providers) {
        InterfaceManager.shared().a = new FirebaseUIInterfaceAdapter(context);
        FirebaseUIModule.shared().setProviders(providers);
    }

    public static FirebaseUIModule shared () {
        return instance;
    }

    public void setProviders (String... providers) {
        for(String provider: providers) {
            idps.add(new AuthUI.IdpConfig.Builder(provider).build());
        }
    }

    public void impl_startAuthenticationActivity (Activity activity) {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(idps)
                        .build(),
                RC_SIGN_IN);
    }

    public void startActivity (Context context, Class firebaseUISubclass) {
        Intent intent = new Intent(context, firebaseUISubclass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void startActivity (Context context) {
        startActivity(context, FirebaseUIActivity.class);
    }
}
