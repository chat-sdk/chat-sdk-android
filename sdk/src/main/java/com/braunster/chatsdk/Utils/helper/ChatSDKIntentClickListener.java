package com.braunster.chatsdk.Utils.helper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by braunster on 26/09/14.
 */
public class ChatSDKIntentClickListener {

    public static View.OnClickListener getPickImageClickListener(final Activity activity, final int requestCode){
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

    public static View.OnClickListener getPickImageClickListener(final Activity activity,final Fragment fragment, final int requestCode){
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

    public static View.OnClickListener getPickImageClickListener(final FragmentActivity activity,final Fragment fragment, final int requestCode){
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

    public static View.OnClickListener getPickImageClickListener(final FragmentActivity activity,final DialogFragment fragment, final int requestCode){
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
