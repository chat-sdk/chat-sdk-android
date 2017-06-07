package co.chatsdk.core.utils;

import android.content.Context;
import android.content.SharedPreferences;

import wanderingdevelopment.tk.chatsdkcore.R;

/**
 * Created by benjaminsmiley-andrews on 04/05/2017.
 */

@Deprecated // Find a better way to implement this
public class AppContext {

    private static final String CHAT_SDK_SHRED_PREFS = "ChatSDK_Prefs";

    public static Context context;

    public static void init(Context ctx) {
        context = ctx.getApplicationContext();
    }

    public static SharedPreferences getPreferences () {
        return context.getSharedPreferences(CHAT_SDK_SHRED_PREFS, Context.MODE_PRIVATE);
    }



}
