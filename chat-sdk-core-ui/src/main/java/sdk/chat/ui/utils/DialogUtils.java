/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.utils;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.StringRes;

public class DialogUtils {

    public static void showToastDialog(Context context, @StringRes int title, @StringRes int message, @StringRes int positive, @StringRes int negative, Runnable positiveAction, Runnable negativeAction) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if (title != 0) {
            builder.setTitle(title);
        }
        if (message != 0) {
            builder.setMessage(message);
        }
        if (positive != 0) {
            builder.setPositiveButton(positive, (dialog, which) -> {
                if (positiveAction != null) {
                    positiveAction.run();
                }
                dialog.dismiss();
            });
        }
        if (negative != 0) {
            builder.setNegativeButton(negative, (dialog, which) -> {
                if (negativeAction != null) {
                    negativeAction.run();
                }
                dialog.dismiss();
            });
        }

        // create alert dialog
        AlertDialog alertDialog = builder.create();

        // show it
        alertDialog.show();
    }
}
