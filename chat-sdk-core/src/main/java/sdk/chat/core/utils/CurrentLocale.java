package sdk.chat.core.utils;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

import sdk.chat.core.session.ChatSDK;

public class CurrentLocale {
    public static Locale get() {
        return get(ChatSDK.ctx());
    }

    public static Locale get(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

}
