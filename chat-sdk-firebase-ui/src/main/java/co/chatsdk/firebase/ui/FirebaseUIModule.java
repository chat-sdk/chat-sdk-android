package co.chatsdk.firebase.ui;

import android.content.Context;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.util.ArrayList;

import co.chatsdk.ui.manager.InterfaceManager;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIModule {

    public static final int RC_SIGN_IN = 900;

    private ArrayList<AuthUI.IdpConfig> idps = new ArrayList<>();
    protected static final FirebaseUIModule instance = new FirebaseUIModule();
    protected Class splashScreenActivity;

    public static void activate (Context context, String... providers) {
        InterfaceManager.shared().a = new FirebaseUIInterfaceAdapter(context);
        FirebaseUIModule.shared().setProviders(providers);
    }

    public static FirebaseUIModule shared () {
        return instance;
    }

    public void setProviders (String... providers) {
        for(String provider: providers) {
            if (provider.equals(GoogleAuthProvider.PROVIDER_ID)) {
                idps.add(new AuthUI.IdpConfig.GoogleBuilder().build());
            }
            if (provider.equals(FacebookAuthProvider.PROVIDER_ID)) {
                idps.add(new AuthUI.IdpConfig.FacebookBuilder().build());
            }
            if (provider.equals(EmailAuthProvider.PROVIDER_ID)) {
                idps.add(new AuthUI.IdpConfig.EmailBuilder().build());
            }
            if (provider.equals(TwitterAuthProvider.PROVIDER_ID)) {
                idps.add(new AuthUI.IdpConfig.TwitterBuilder().build());
            }
            if (provider.equals(PhoneAuthProvider.PROVIDER_ID)) {
                idps.add(new AuthUI.IdpConfig.PhoneBuilder().build());
            }
            if (provider.equals(GithubAuthProvider.PROVIDER_ID)) {
                idps.add(new AuthUI.IdpConfig.GitHubBuilder().build());
            }
        }
    }

    /**
     * If you change the splash screen activity, you will also need to update the
     * code in the Android Manifest to match
     * @param activity
     */
    public void setSplashScreen (Class<? extends SplashScreenActivity> activity) {
        splashScreenActivity = activity;
    }

    public Class<? extends SplashScreenActivity> getSplashScreenActivity () {
        return splashScreenActivity != null ? splashScreenActivity : SplashScreenActivity.class;
    }

    public ArrayList<AuthUI.IdpConfig> getIdps () {
        return idps;
    }

}
