/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:32 PM
 */

package sdk.chat.ui.activities;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.ActivityResult;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.AlertUtils;
import sdk.guru.common.DisposableMap;

public abstract class BaseActivity extends AppCompatActivity implements Consumer<Throwable>, CompletableObserver {

    // This is a list of extras that are passed to the login view
    protected HashMap<String, Object> extras = new HashMap<>();
    protected DisposableMap dm = new DisposableMap();

    protected AlertUtils alert;

    public BaseActivity() {
        alert = new AlertUtils(new AlertUtils.Provider() {
            @Override
            public Context getContext() {
                return BaseActivity.this;
            }
            @Override
            public View getRootView() {
                return getContentView();
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(UIModule.config().theme != 0) {
            setTheme(UIModule.config().theme);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        setContentView(getLayout());
        ButterKnife.bind(this);

        Logger.debug("onCreate: " + this);

        updateExtras(getIntent().getExtras());


        // Setting the default task description.
        if (getTaskDescriptionBitmap() != null) {
            setTaskDescription(getTaskDescriptionBitmap(), getTaskDescriptionLabel(), getTaskDescriptionColor());
        }
    }

    protected void initViews() {
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    protected @Nullable Toolbar getToolbar() {
        return findViewById(R.id.toolbar);
    }

    protected @Nullable View getContentView() {
        return findViewById(R.id.content);
    }

    protected void setActionBarTitle (int resourceId) {
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(getString(resourceId));
            ab.setHomeButtonEnabled(true);
        }
    }

    protected abstract @LayoutRes int getLayout();

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    /**
     * @return the bitmap that will be used for the screen overview also called the recents apps.
     **/
    protected Bitmap getTaskDescriptionBitmap(){
        if (ChatSDK.config() != null) {
            return BitmapFactory.decodeResource(getResources(), ChatSDK.config().logoDrawableResourceID);
        }
        return null;
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

    protected void updateExtras (@Nullable Bundle bundle) {
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
        dm.dispose();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        for (String key: extras.keySet()) {
            Object extra = extras.get(key);
            if (extra instanceof String) {
                outState.putString(key, (String) extra);
            }
            if (extra instanceof Integer) {
                outState.putInt(key, (Integer) extra);
            }
            if (extra instanceof Float) {
                outState.putFloat(key, (Float) extra);
            }
            if (extra instanceof Double) {
                outState.putDouble(key, (Double) extra);
            }
            if (extra instanceof Long) {
                outState.putLong(key, (Long) extra);
            }
        }
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

        //Set up touch listener for non-text box views to hideName keyboard.
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ActivityResultPushSubjectHolder.shared().accept(new ActivityResult(requestCode, resultCode, data));
    }

    public void hideKeyboard() {
        BaseActivity.hideKeyboard(this);
    }

    public static void hideKeyboard(@Nullable Activity activity) {
        if (activity != null) {
            View view = activity.getCurrentFocus();
            if (view != null) {
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputManager != null) {
                    inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        }
    }

    public void showKeyboard() {
        if(!KeyboardVisibilityEvent.INSTANCE.isKeyboardVisible(this)) {
            BaseActivity.showKeyboard(this);
        }
    }

    public static void showKeyboard(@Nullable Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
    }

    /**
     * Some convenience methods to handle disposables and errors
     */

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();  // optional depending on your needs
    }

    @Override
    public void accept(Throwable throwable) {
        onError(throwable);
    }

    @Override
    public void onSubscribe(Disposable d) {
        dm.add(d);
    }

    @Override
    public void onComplete() {

    }

    @Override
    public void onError(Throwable e) {
        if (ChatSDK.config().debug) {
            e.printStackTrace();
        }
        alert.onError(e);
    }

    /** Show a SuperToast with the given text. */
    public void showToast(@StringRes int textResourceId){
        alert.showToast(textResourceId);
    }

    public void showToast(String text){
        alert.showToast(text);
    }

    public void showSnackbar(int textResourceId, int duration){
        alert.showSnackbar(textResourceId, duration);
    }

    public void showSnackbar(int textResourceId){
        alert.showSnackbar(textResourceId);
    }

    public void showSnackbar (String text) {
        alert.showSnackbar(text);
    }

    public void showSnackbar (String text, int duration) {
        alert.showSnackbar(text, duration);
    }

    protected Consumer<? super Throwable> toastOnErrorConsumer () {
        return alert.toastOnErrorConsumer();
    }

    protected Consumer<? super Throwable> snackbarOnErrorConsumer () {
        return alert.snackbarOnErrorConsumer();
    }

    protected void showProgressDialog(int stringResId) {
        alert.showProgressDialog(stringResId);
    }

    protected void showProgressDialog(String message) {
        alert.showProgressDialog(message);
    }

    protected AlertDialog getProgressDialog() {
        return alert.getProgressDialog();
    }

    protected void showOrUpdateProgressDialog(String message) {
        alert.showOrUpdateProgressDialog(message);
    }

    protected void dismissProgressDialog() {
        alert.dismissProgressDialog();
    }
}
