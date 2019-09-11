package co.chatsdk.firebase.ui;

import android.content.Context;
import android.content.Intent;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.util.ArrayList;

import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebaseCoreHandler;
import co.chatsdk.firebase.FirebaseEventHandler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIModule {

    public static void activate (String... providers) {

        ArrayList<AuthUI.IdpConfig> idps = getProviders(providers);

        Intent authUILoginIntent = authUI()
                .createSignInIntentBuilder()
                .setAvailableProviders(idps)
                .build();

        ChatSDK.ui().setLoginIntent(authUILoginIntent);

        Disposable d = ChatSDK.events().source()
                .filter(NetworkEvent.filterType(EventType.Logout))
                .subscribe(networkEvent -> authUI().signOut(ChatSDK.shared().context()));

    }

    public static ArrayList<AuthUI.IdpConfig> getProviders (String... providers) {
        ArrayList<AuthUI.IdpConfig> idps = new ArrayList<>();

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
        return idps;
    }

    public static AuthUI authUI () {
        return AuthUI.getInstance(FirebaseCoreHandler.app());
    }

}
