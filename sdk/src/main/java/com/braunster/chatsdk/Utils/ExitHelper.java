/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.network.BDefines;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.concurrent.Callable;

/**
 * Helper to create special exit to your app.
 * See {@link com.braunster.chatsdk.network.BDefines.Exit} for setting the app exit action.
 *
 * Example for it's use is in {@link com.braunster.chatsdk.activities.ChatSDKMainActivity#exitHelper MainActivity exit helper}
 */
public class ExitHelper {

    private Activity activity;
    private boolean doubleBackToExitPressedOnce = false;
    private SuperToast superToast;

    public ExitHelper(Activity activity){
        this.activity = activity;
        superToast = new SuperToast(activity);
    }

    public void triggerExit() {
        switch (BDefines.Defaults.SDKExitMode)
        {
            case BDefines.Exit.EXIT_MODE_DIALOG:
                // Show alert dialog, Positive response is just dismiss the dialog, Negative will close the app.
                DialogUtils.showAlertDialog(activity, "", activity.getResources().getString(R.string.alert_exit), activity.getResources().getString(R.string.exit),
                        activity.getResources().getString(R.string.stay), null, new CloseApp());
                break;

            case BDefines.Exit.EXIT_MODE_DOUBLE_BACK:
                if (doubleBackToExitPressedOnce) {
                    try {
                        new CloseApp().call();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, BDefines.Exit.DOUBLE_CLICK_INTERVAL);

                this.doubleBackToExitPressedOnce = true;
                showAlertToast( activity.getString(R.string.exit_helper_double_tap_toast) );
                break;

            case BDefines.Exit.EXIT_MODE_NONE:
                try {
                    new CloseApp().call();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                break;
        }
    }

    /** Close the app when called.*/
    class CloseApp implements Callable {
        @Override
        public Object call() throws Exception {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            return null;
        }
    }

    protected void showAlertToast(String text){
        superToast.setDuration(SuperToast.Duration.MEDIUM);
        superToast.setTextColor(Color.WHITE);
        superToast.setAnimations(SuperToast.Animations.FLYIN);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setText(text);
        superToast.show();
    }

}
