/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.helpers;

import android.app.AlertDialog;
import android.content.Context;

import java.util.concurrent.Callable;

import co.chatsdk.core.session.ChatSDK;

@Deprecated
public class DialogUtils {

    public static void showToastDialog(Context context, String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set title if not null
        if (title != null && !title.equals("")) {
            alertDialogBuilder.setTitle(title);
        }

        // set dialog message
        alertDialogBuilder
                .setMessage(alert)
                .setCancelable(false)
                .setPositiveButton(p, (dialog, id) -> {
                    if (pos != null)
                        try {
                            pos.call();
                        } catch (Exception e) {
                            ChatSDK.logError(e);
                        }
                    dialog.dismiss();
                })
                .setNegativeButton(n, (dialog, id) -> {
                    // if this button is clicked, just close
                    // the dialog box and do nothing
                    if (neg != null)
                        try {
                            neg.call();
                        } catch (Exception e) {
                            ChatSDK.logError(e);
                        }

                    dialog.cancel();
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
}
