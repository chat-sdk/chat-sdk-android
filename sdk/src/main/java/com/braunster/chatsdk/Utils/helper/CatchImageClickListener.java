/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.helper;

import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

public class CatchImageClickListener implements View.OnClickListener{
    /** The key to get the path of the last captured image path in case the activity is destroyed while capturing.*/
    public static final String SELECTED_FILE_PATH = "captured_photo_path";

    private AppCompatActivity activity;
    private DialogFragment dialogFragment;
    private Fragment fragment;
    private int requestCode;

    private File file = null;

    private String selectedFilePath = "";

    public CatchImageClickListener(AppCompatActivity activity, DialogFragment dialogFragment, int requestCode) {
        this.activity = activity;
        this.dialogFragment = dialogFragment;
        this.requestCode = requestCode;
    }

    public CatchImageClickListener(AppCompatActivity activity, int requestCode) {
        this.activity = activity;
        this.requestCode = requestCode;
    }

    public CatchImageClickListener(AppCompatActivity activity, int requestCode, Fragment fragment) {
        this.activity = activity;
        this.requestCode = requestCode;
        this.fragment = fragment;
    }

    @Override
    public void onClick(View v) {
        catchIntent();
    }

    private void catchIntent(){
        if (file == null)
            throw new NullPointerException("You need to set the file you want to save the image in.");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        selectedFilePath = file.getPath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));

        if (activity==null)
            return;

        // start the image capture Intent
        if (dialogFragment != null)
            activity.startActivityFromFragment(dialogFragment, intent, requestCode);
        else if (fragment!= null)
            activity.startActivityFromFragment(fragment, intent, requestCode);
        else activity.startActivityForResult(intent, requestCode);
    }

    public void onSavedInstanceBundle(Bundle outState){
        if (StringUtils.isNotEmpty(selectedFilePath))
        {
            outState.putString(SELECTED_FILE_PATH, selectedFilePath);
        }
    }

    public void restoreSavedInstance(Bundle savedInstanceState){
        if (savedInstanceState == null)
            return;

        selectedFilePath = savedInstanceState.getString(SELECTED_FILE_PATH);
        savedInstanceState.remove(SELECTED_FILE_PATH);
    }

    public String getSelectedFilePath() {
        return selectedFilePath;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setDialogFragment(DialogFragment dialogFragment) {
        this.dialogFragment = dialogFragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

}