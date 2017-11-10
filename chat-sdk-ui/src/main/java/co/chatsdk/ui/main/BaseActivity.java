/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:32 PM
 */

package co.chatsdk.ui.main;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.AppBackgroundMonitor;
import co.chatsdk.ui.utils.ToastHelper;

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;

    public BaseActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setting the default task description.
        setTaskDescription(getTaskDescriptionBitmap(), getTaskDescriptionLabel(), getTaskDescriptionColor());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * @return the bitmap that will be used for the screen overview also called the recents apps.
     **/
    protected Bitmap getTaskDescriptionBitmap(){
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    }

    protected int getTaskDescriptionColor(){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    protected String getTaskDescriptionLabel(){
        return (String) getTitle();
    }
    
    protected void setTaskDescription(Bitmap bm, String label, int color){
        // Color the app topbar label and icon in the overview screen
        //http://www.bignerdranch.com/blog/polishing-your-Android-overview-screen-entry/
        // Placed in the post create so it would be called after the action bar is initialized and we have a title.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(label, bm, color);

            setTaskDescription(td);
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }



    @Override
    protected void onResume() {
        super.onResume();
        AppBackgroundMonitor.shared().stopActivityTransitionTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppBackgroundMonitor.shared().startActivityTransitionTimer();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissProgressDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.
     * http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
     * */
    public void setupTouchUIToDismissKeyboard(View view) {
        setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(BaseActivity.this);
                return false;
            }
        }, -1);
    }

    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, final Integer... exceptIDs) {
        List<Integer> ids = new ArrayList<>();
        if (exceptIDs != null)
            ids = Arrays.asList(exceptIDs);

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            if (!ids.isEmpty() && ids.contains(view.getId()))
            {
                return;
            }

            view.setOnTouchListener(onTouchListener);
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupTouchUIToDismissKeyboard(innerView, onTouchListener, exceptIDs);
            }
        }
    }

    /** Hide the Soft Keyboard.*/
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null)
            return;

        if (activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    /** Show a SuperToast with the given text. */
    protected void showToast(String text){
        if (StringUtils.isEmpty(text))
            return;

        ToastHelper.show(this, text);
    }

    protected void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }

        if (!progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void showOrUpdateProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }

        if (!progressDialog.isShowing()) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        } else progressDialog.setMessage(message);
    }

    protected void dismissProgressDialog() {
        // For handling orientation changed.
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


