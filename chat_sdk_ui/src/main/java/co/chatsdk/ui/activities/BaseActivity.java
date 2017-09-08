/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:32 PM
 */

package co.chatsdk.ui.activities;

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

import co.chatsdk.core.NM;

import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import co.chatsdk.core.defines.Debug;


import org.apache.commons.lang3.StringUtils;

import timber.log.Timber;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends AppCompatActivity {

    private static final boolean DEBUG = Debug.BaseActivity;

    public static final String FROM_LOGIN = "FROM_LOGIN";

    private ProgressDialog progressDialog;

    /** If true the activity will implement facebook SDK components like sessionChangeState and the facebook UI helper.
     * This is good for caching a press on the logout button in the main activity or in any activity that will implement the facebook login button.*/
    protected boolean integratedWithFacebook = false;

    /** A flag indicates that the activity in opened from the login activity so we wont do auth check when the activity will get to the onResume state.*/
    boolean fromLoginActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getExtras() != null) {
            fromLoginActivity = getIntent().getExtras().getBoolean(FROM_LOGIN, false);
            // So we won't encounter this flag again.
            getIntent().removeExtra(FROM_LOGIN);
        }
        else {
            fromLoginActivity = false;
        }

        if (savedInstanceState != null) {
            fromLoginActivity = savedInstanceState.getBoolean(FROM_LOGIN, false);
        }

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
        if (intent != null && intent.getExtras() != null)
        {
            if (DEBUG) Timber.d("From login");
            fromLoginActivity = intent.getExtras().getBoolean(FROM_LOGIN, false);
            // So we wont encounter this flag again.
            intent.removeExtra(FROM_LOGIN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fromLoginActivity = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        NM.core().setUserOnline();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NM.core().setUserOffline();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
//            uiHelper.onDestroy();

        dismissProgressDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FROM_LOGIN, fromLoginActivity);
    }

    /**
     * Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.
     * http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
     * */
    public void setupTouchUIToDismissKeyboard(View view) {
        UIHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(BaseActivity.this);
                return false;
            }
        });
    }

    public void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, final Integer... exceptIDs) {
        UIHelper.setupTouchUIToDismissKeyboard(view, onTouchListener, exceptIDs);
    }

    /** Hide the Soft Keyboard.*/
    public static void hideSoftKeyboard(AppCompatActivity activity) {
        UIHelper.hideSoftKeyboard(activity);
    }

    /** Show a SuperToast with the given text. */
    protected void showToast(String text){
        if (StringUtils.isEmpty(text))
            return;

        ToastHelper.show(text);
    }

    /** Authenticates the current user.*/
    public Completable authenticate(){
        return NM.auth().authenticateWithCachedToken();
    }

    protected void showProgressDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void showOrUpdateProgressDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        } else progressDialog.setMessage(message);
    }

    protected void dismissProgressDialog(){
        // For handling orientation changed.
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enableFacebookIntegration(boolean integratedWithFacebook) {
        this.integratedWithFacebook = integratedWithFacebook;
    }

}


