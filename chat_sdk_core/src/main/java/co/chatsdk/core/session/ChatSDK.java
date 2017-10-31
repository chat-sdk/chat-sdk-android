package co.chatsdk.core.session;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.ref.WeakReference;

import co.chatsdk.core.dao.DaoCore;
import timber.log.Timber;

/**
 * Created by ben on 9/5/17.
 */

public class ChatSDK {

    public static String Preferences = "chat_sdk_preferences";

    private static final ChatSDK instance = new ChatSDK();
    public WeakReference<Context> context;
    public Configuration config;

    protected ChatSDK () {
    }

    private void setContext (Context context) {
        this.context = new WeakReference<>(context);
        DaoCore.init(context);
    }

    public static ChatSDK initialize (Configuration config) {
        shared().setContext(config.context.get());
        shared().config = config;


//        if (debug) {
        // TODO: Update this
            Timber.plant(new Timber.DebugTree());
//        } else {
//            Timber.plant(new Timber.Tree());
//        }

        return shared();
    }

    public static ChatSDK shared () {
        return instance;
    }

    public SharedPreferences getPreferences () {
        return context.get().getSharedPreferences(Preferences, Context.MODE_PRIVATE);
    }

    public Context context () {
        return context.get();
    }

    public static Configuration config () {
        return shared().config;
    }

}
