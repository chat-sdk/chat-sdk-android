package co.chatsdk.firebase.nearby_users;

import android.content.Context;

import androidx.fragment.app.Fragment;

import sdk.chat.core.Tab;
import sdk.chat.core.handlers.Module;
import sdk.guru.common.BaseConfig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;


/**
 * Created by pepe on 08.03.18.
 */

public class FirebaseNearbyUsersModule implements Module {

    public static final FirebaseNearbyUsersModule instance = new FirebaseNearbyUsersModule();

    public static FirebaseNearbyUsersModule shared() {
        return instance;
    }

    public static Config<FirebaseNearbyUsersModule> configure() {
        return instance.config;
    }

    public static FirebaseNearbyUsersModule configure(Configure<Config> config) {
        config.with(instance.config);
        return instance;
    }

    public Config<FirebaseNearbyUsersModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        ChatSDK.ui().addTab(nearbyUsersTab(), 2);
    }

    @Override
    public String getName() {
        return "FirebaseNearbyUsersModule";
    }

    public static class Config<T> extends BaseConfig<T> {

        // Maximum distance to pick up nearby users
        public int maxDistance = 50000;

        // How much distance must be moved to update the server with our new location
        public int minimumLocationChangeToUpdateServer = 50;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> nearbyUserMaxDistance (int maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        public Config<T> nearbyUsersMinimumLocationChangeToUpdateServer (int minimumDistance) {
            this.minimumLocationChangeToUpdateServer = minimumDistance;
            return this;
        }

    }

    public static Tab nearbyUsersTab() {
        Context context = ChatSDK.shared().context();
        return new Tab(R.string.nearby_users, context.getResources().getDrawable(R.drawable.nearby_users), nearbyUsersFragment());
    }

    public static Fragment nearbyUsersFragment() {
        return NearbyUsersFragment.newInstance();
    }

    public static Config config() {
        return shared().config;
    }
}
