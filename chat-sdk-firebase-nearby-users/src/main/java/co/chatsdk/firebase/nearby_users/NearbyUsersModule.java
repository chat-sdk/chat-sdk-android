package co.chatsdk.firebase.nearby_users;

import android.content.Context;

import co.chatsdk.core.session.InterfaceManager;

/**
 * Created by pepe on 08.03.18.
 */

public class NearbyUsersModule {

    public static void activate (Context context) {
        InterfaceManager.shared().a = new NearbyUsersInterfaceAdapter(context);
    }

}
