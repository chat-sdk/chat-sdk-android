/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.utils;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;


public class ChatSDKIntentClickListener {

    public static View.OnClickListener getPickImageClickListener(final AppCompatActivity activity, final int requestCode){
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent();
            }

            private void pickIntent(){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activity.startActivityForResult(Intent.createChooser(intent,
                        "Complete action using"), requestCode);
            }
        };
    }

    public static View.OnClickListener getPickImageClickListener(final AppCompatActivity activity,final Fragment fragment, final int requestCode){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent();
            }

            private void pickIntent(){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activity.startActivityFromFragment(fragment, Intent.createChooser(intent,
                        "Complete action using"), requestCode);
            }
        };
    }

    public static View.OnClickListener getPickImageClickListener(final AppCompatActivity activity, final DialogFragment fragment, final int requestCode){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent();
            }

            private void pickIntent(){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activity.startActivityFromFragment(fragment, Intent.createChooser(intent,
                        "Complete action using"), requestCode);
            }
        };
    }

}
