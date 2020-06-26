package sdk.chat.firebase.ui;

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

import io.reactivex.Completable;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.firebase.adapter.FirebaseCoreHandler;
import sdk.guru.common.BaseConfig;

/**
 * Created by ben on 1/2/18.
 */

public class FirebaseUIModule extends AbstractModule {

    public static final FirebaseUIModule instance = new FirebaseUIModule();

    public FirebaseUIModule() {
    }

    public static FirebaseUIModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<FirebaseUIModule> builder() {
        return instance.config;
    }

    public static FirebaseUIModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {
        public List<String> providers = new ArrayList<>();

        public Config(T onBuild) {
            super(onBuild);
        }

        /**
         * Set available providers
         * @param providers
         * @return
         */
        public Config<T> setProviders(String... providers) {
            this.providers.addAll(Arrays.asList(providers));
            return this;
        }

    }

    protected Config<FirebaseUIModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {

        ArrayList<AuthUI.IdpConfig> idps = getProviders(config.providers);

        Intent authUILoginIntent = authUI()
                .createSignInIntentBuilder()
                .setLogo(ChatSDK.config().logoDrawableResourceID)
                .setTheme(R.style.ChatSDKTheme)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(idps)
                .build();

        ChatSDK.ui().setLoginIntent(authUILoginIntent);

//        dm.add(ChatSDK.events().source()
//                .filter(NetworkEvent.filterType(EventType.Logout))
//                .subscribe(networkEvent -> authUI().signOut(context)));

        ChatSDK.hook().addHook(Hook.async(data -> Completable.create(emitter -> {
            authUI().signOut(ChatSDK.ctx()).addOnSuccessListener(aVoid -> emitter.onComplete()).addOnFailureListener(emitter::onError);
        })), HookEvent.DidLogout);

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
                idps.add(new AuthUI.IdpConfig.EmailBuilder()
                        .setRequireName(false).build());
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

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
