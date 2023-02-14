package sdk.chat.ui.utils;

import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.widget.Toast;

import androidx.annotation.StringRes;

/**
 * Created by ben on 9/8/17.
 */

public class ToastHelper {

    public static void show(Context context, String text) {
        if(text != null && !text.isEmpty()) {
//            Toast.makeText(context, text, Toast.LENGTH_LONG).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Toast.makeText(
                        context,
                        Html.fromHtml("<font color='#e3f2fd' ><b>" + text + "</b></font>", 0),
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        context,
                        Html.fromHtml("<font color='#e3f2fd' ><b>" + text + "</b></font>"),
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    public static void show(Context context, @StringRes int resourceId){
        show(context, context.getString(resourceId));
    }

}
