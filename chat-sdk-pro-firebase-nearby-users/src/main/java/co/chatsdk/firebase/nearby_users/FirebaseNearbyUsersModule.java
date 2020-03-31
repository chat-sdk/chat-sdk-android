package co.chatsdk.firebase.nearby_users;

import android.content.Context;

import androidx.fragment.app.Fragment;
import co.chatsdk.core.Tab;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configure;

/**
 * Created by pepe on 08.03.18.
 */

public class FirebaseNearbyUsersModule implements Module {

    public static final FirebaseNearbyUsersModule instance = new FirebaseNearbyUsersModule();

    public static FirebaseNearbyUsersModule shared() {
        return instance;
    }

    public static FirebaseNearbyUsersModule shared(Configure<Config> configure) {
        configure.with(instance.config);
        return instance;
    }

    public Config config = new Config();

    @Override
    public void activate(Context context) {
        ChatSDK.ui().addTab(nearbyUsersTab(), 2);
    }

    @Override
    public String getName() {
        return "FirebaseNearbyUsersModule";
    }

    public static class Config {

        // Maximum distance to pick up nearby users
        public int maxDistance = 50000;

        // How much distance must be moved to update the server with our new location
        public int minimumLocationChangeToUpdateServer = 50;

        public Config nearbyUserMaxDistance (int maxDistance) {
            this.maxDistance = maxDistance;
            return this;
        }

        public Config nearbyUsersMinimumLocationChangeToUpdateServer (int minimumDistance) {
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
