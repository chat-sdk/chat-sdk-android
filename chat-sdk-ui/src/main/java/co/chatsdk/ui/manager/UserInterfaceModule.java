package co.chatsdk.ui.manager;

import android.content.Context;

/**
 * Created by ben on 10/27/17.
 */

public class UserInterfaceModule {

    public static void activate (Context context) {
        InterfaceManager.shared().a = new BaseInterfaceAdapter(context);
    }
}
