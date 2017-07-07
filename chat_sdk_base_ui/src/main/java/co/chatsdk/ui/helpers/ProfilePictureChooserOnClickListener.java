package co.chatsdk.ui.helpers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.lang.ref.WeakReference;

import co.chatsdk.ui.profile.AbstractProfileFragment;

/**
 * Created by benjaminsmiley-andrews on 05/07/2017.
 */

public class ProfilePictureChooserOnClickListener implements View.OnClickListener {

    public static final int PROFILE_PIC = 100;

    WeakReference<AppCompatActivity> activity;
    WeakReference<Fragment> fragment;

    public ProfilePictureChooserOnClickListener(AppCompatActivity activity) {
        this(activity, null);
    }

    public ProfilePictureChooserOnClickListener(AppCompatActivity activity, Fragment fragment) {
        this.activity = new WeakReference<AppCompatActivity>(activity);
        this.fragment = new WeakReference<Fragment>(fragment);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        if(fragment.get() != null) {
            activity.get().startActivityFromFragment(fragment.get(), Intent.createChooser(intent,
                    "Complete action using"), PROFILE_PIC);
        }
        else {
            activity.get().startActivityForResult(Intent.createChooser(intent,
                    "Complete action using"), PROFILE_PIC);
        }

    }

}
