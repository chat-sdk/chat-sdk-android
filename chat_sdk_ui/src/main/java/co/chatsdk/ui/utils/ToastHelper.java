package co.chatsdk.ui.utils;

import android.support.annotation.StringRes;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import co.chatsdk.core.utils.AppContext;

/**
 * Created by ben on 9/8/17.
 */

public class ToastHelper {

    public static void show(String text) {
        if(!StringUtils.isEmpty(text)) {
            Toast.makeText(AppContext.shared().context(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public static void show(@StringRes int resourceId){
        show(AppContext.shared().context().getString(resourceId));
    }

}
