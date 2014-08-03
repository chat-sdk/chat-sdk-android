package com.braunster.chatsdk.Utils.volley;

import android.content.Context;
import android.graphics.Color;

import com.github.johnpersano.supertoasts.SuperToast;

/**
 * Created by braunster on 29/07/14.
 */
public class ChatSDKToast {

    public static void toastAlert(Context context, String alert){
        toastAlert(context, alert, -10000);
    }

    public static void toastAlert(Context context, String text, int gravity){
        SuperToast superToast = new SuperToast(context);
        superToast.setDuration(SuperToast.Duration.MEDIUM);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setTextColor(Color.WHITE);
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.setText(text);

        if (gravity != -10000)
            superToast.setGravity(gravity, 0, 0);

        superToast.show();
    }

    public static void toast(Context context, String text){
        SuperToast superToast = new SuperToast(context);
        superToast.setDuration(SuperToast.Duration.MEDIUM);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.setText(text);
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.show();
    }

}
