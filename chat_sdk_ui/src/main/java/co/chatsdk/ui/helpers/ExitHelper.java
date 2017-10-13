/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.helpers;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.concurrent.Callable;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.ui.R;

/**
 * Helper to create special exit to your app.
 * See {@link Keys.Exit} for setting the app exit action.
 *
 */

@Deprecated
public class ExitHelper {

    private AppCompatActivity activity;
    private boolean doubleBackToExitPressedOnce = false;

    public ExitHelper(AppCompatActivity activity){
        this.activity = activity;
    }

    public void triggerExit() {
        switch (Keys.Defaults.SDKExitMode)
        {
            case Keys.Exit.EXIT_MODE_DIALOG:
                // Show alert dialog, Positive response is just dismiss the dialog, Negative will close the app.
                DialogUtils.showToastDialog(activity, "", activity.getResources().getString(R.string.alert_exit), activity.getResources().getString(R.string.exit),
                        activity.getResources().getString(R.string.stay), null, new CloseApp());
                break;

            case Keys.Exit.EXIT_MODE_DOUBLE_BACK:
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
                }, Keys.Exit.DOUBLE_CLICK_INTERVAL);

                this.doubleBackToExitPressedOnce = true;
                showToast( activity.getString(R.string.exit_helper_double_tap_toast) );
                break;

            case Keys.Exit.EXIT_MODE_NONE:
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

    protected void showToast(String text) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
    }

}
