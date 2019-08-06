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
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.functions.Consumer;

public class BaseActivity extends AppCompatActivity {

    protected ProgressDialog progressDialog;

    // This is a list of extras that are passed to the login view
    protected HashMap<String, Object> extras = new HashMap<>();
    protected DisposableList disposableList = new DisposableList();

    public BaseActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateExtras(getIntent().getExtras());

        // Setting the default task description.
        setTaskDescription(getTaskDescriptionBitmap(), getTaskDescriptionLabel(), getTaskDescriptionColor());
    }

    protected void setActionBarTitle (int resourceId) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(getString(resourceId));
            ab.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * @return the bitmap that will be used for the screen overview also called the recents apps.
     **/
    protected Bitmap getTaskDescriptionBitmap(){
        return BitmapFactory.decodeResource(getResources(), ChatSDK.config().logoDrawableResourceID);
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

    protected void updateExtras (Bundle bundle) {
        if (bundle != null) {
            for (String s : bundle.keySet()) {
                extras.put(s, bundle.get(s));
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateExtras(intent.getExtras());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        disposableList.dispose();
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
        setupTouchUIToDismissKeyboard(view, (v, event) -> {
            hideKeyboard();
            return false;
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

    /** Show a SuperToast with the given text. */
    protected void showToast(int textResourceId){
        showToast(this.getString(textResourceId));
    }

    protected void showToast(String text){
        if (!text.isEmpty()) {
            ToastHelper.show(this, text);
        }
    }

    protected void showSnackbar(int textResourceId){
        showSnackbar(this.getString(textResourceId));
    }

    protected void showSnackbar (String text) {
        if (!text.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_SHORT).show();
        }
    }

    protected Consumer<? super Throwable> toastOnErrorConsumer () {
        return (Consumer<Throwable>) throwable -> showToast(throwable.getLocalizedMessage());
    }

    protected Consumer<? super Throwable> snackbarOnErrorConsumer () {
        return (Consumer<Throwable>) throwable -> showSnackbar(throwable.getLocalizedMessage());
    }

    protected void showProgressDialog(int stringResId) {
        showProgressDialog(getString(stringResId));
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
        } else {
            progressDialog.setMessage(message);
        }
    }

    protected void dismissProgressDialog() {
        // For handling orientation changed.
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            ChatSDK.logError(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequestHandler.shared().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultPushSubjectHolder.shared().onNext(new ActivityResult(requestCode, resultCode, data));
    }

    public void hideKeyboard() {
        BaseActivity.hideKeyboard(this);
    }

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();  // optional depending on your needs
    }

}
