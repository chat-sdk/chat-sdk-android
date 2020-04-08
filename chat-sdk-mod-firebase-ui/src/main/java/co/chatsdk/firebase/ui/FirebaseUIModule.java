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
import java.util.Arrays;
import java.util.List;

import chatsdk.co.chat_sdk_firebase_ui.R;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.handlers.Module;
import sdk.guru.common.BaseConfig;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;
import sdk.guru.common.DisposableMap;
import co.chatsdk.firebase.FirebaseCoreHandler;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIModule implements Module {

    public static final FirebaseUIModule instance = new FirebaseUIModule();

    public static FirebaseUIModule shared() {
        return instance;
    }

    public static Config<FirebaseUIModule> configure() {
        return instance.config;
    }

    public static FirebaseUIModule configure(Configure<Config> config) {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {
        public List<String> providers = new ArrayList<>();

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> setProviders(String... providers) {
            this.providers.addAll(Arrays.asList(providers));
            return this;
        }
    }

    DisposableMap dm = new DisposableMap();

    protected Config<FirebaseUIModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {

        ArrayList<AuthUI.IdpConfig> idps = getProviders(config.providers);

        Intent authUILoginIntent = authUI()
                .createSignInIntentBuilder()
                .setLogo(ChatSDK.config().logoDrawableResourceID)
                .setTheme(R.style.FirebaseLoginTheme)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(idps)
                .build();

        ChatSDK.ui().setLoginIntent(authUILoginIntent);

        dm.add(ChatSDK.events().source()
                .filter(NetworkEvent.filterType(EventType.Logout))
                .subscribe(networkEvent -> authUI().signOut(context)));
    }

    protected ArrayList<AuthUI.IdpConfig> getProviders (List<String> providers) {
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

    protected AuthUI authUI () {
        return AuthUI.getInstance(FirebaseCoreHandler.app());
    }

    @Override
    public String getName() {
        return "FirebaseUIModule";
    }

}
