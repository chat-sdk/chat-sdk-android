package co.chatsdk.ui.utils;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by ben on 9/8/17.
 */

public class ToastHelper {

    public static void show(Context context, String text) {
        if(!StringUtils.isEmpty(text)) {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void show(Context context, @StringRes int resourceId){
        show(context, context.getString(resourceId));
    }

}
