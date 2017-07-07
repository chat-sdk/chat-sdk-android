/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import co.chatsdk.ui.profile.EditProfileActivity;
import co.chatsdk.ui.activities.PickFriendsActivity;
import co.chatsdk.ui.activities.ShareWithContactsActivity;
import co.chatsdk.ui.threads.ThreadDetailsActivity;
import co.chatsdk.core.defines.Debug;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.activities.AbstractLoginActivity;
import co.chatsdk.ui.activities.LoginActivity;
import co.chatsdk.ui.activities.MainActivity;
import co.chatsdk.ui.activities.SearchActivity;

public class UIHelper {

    public static final String TAG = UIHelper.class.getSimpleName();
    public static final boolean DEBUG = Debug.UiUtils;

    public static final String USER_ID = "user_id";
    public static final String USER_ENTITY_ID = "user_entity_id";

    private SuperToast toast, alertToast;
    private SuperCardToast superCardToastProgress;

    private WeakReference<Context> context;

    private Class chatActivity;
    private Class mainActivity;
    private Class loginActivity;
    private Class searchActivity = SearchActivity.class;
    private Class pickFriendsActivity = PickFriendsActivity.class;
    private Class shareWithFriendsActivity = ShareWithContactsActivity.class;
    private Class editProfileActivity= EditProfileActivity.class;
    private Class profileActivity = null;
    private Class threadDetailsActivity = ThreadDetailsActivity.class;

    private final static UIHelper instance = new UIHelper();

    protected UIHelper () {
        chatActivity = ChatActivity.class;
        mainActivity = MainActivity.class;
        loginActivity = LoginActivity.class;
    }

    public static UIHelper getInstance () {
        return instance;
    }


    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.*/
    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener) {
        setupTouchUIToDismissKeyboard(view, onTouchListener, -1);
    }

    public void setContext (Context context) {
        this.context = new WeakReference<Context>(context);
    }

    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, Integer... exceptIDs) {

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
    public static void hideSoftKeyboard(AppCompatActivity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE);

        if (inputMethodManager == null)
            return;

        if (activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null)
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }


    public Class getLoginActivity() {
        return loginActivity;
    }

    public Class getMainActivity() {
        return mainActivity;
    }

    public Class getChatActivity() {
        return chatActivity;
    }

    public Class getThreadDetailsActivity() {
        return threadDetailsActivity;
    }

    public Class getPickFriendsActivity() {
        return pickFriendsActivity;
    }

    public Class getSearchActivity() {
        return searchActivity;
    }

    /** Start the chat activity for given thread id.*/
    public void startChatActivityForID(long id){

        if (colleted())
            return;

        Intent intent = new Intent(context.get(), chatActivity);
        intent.putExtra(ChatActivity.THREAD_ID, id);

        startActivity(intent);
    }

    public void startLoginActivity(boolean loggedOut){
        Intent intent = new Intent(context.get(), loginActivity);
        intent.putExtra(AbstractLoginActivity.FLAG_LOGGED_OUT, loggedOut);
        startActivity(intent);
    }

    public void startMainActivity(){
        startActivity(mainActivity);
    }

    public void startSearchActivity(){
        startActivity(searchActivity);
    }

    public void startPickFriendsActivity(){
        startActivity(pickFriendsActivity);
    }

    public void startShareWithFriendsActivity(){
        if (colleted())
            return;

        startActivity(shareWithFriendsActivity);
    }

    public boolean startProfileActivity(String entityId){

        if (colleted())
            return false;

        if (profileActivity==null)
            return false;

        Intent intent = new Intent(context.get(), profileActivity);
        intent.putExtra(USER_ENTITY_ID, entityId);

        startActivity(intent);

        return true;
    }

    public boolean startProfileActivity(long id){

        if (colleted())
            return false;

        if (profileActivity==null)
            return false;

        Intent intent = new Intent(context.get(), profileActivity);
        intent.putExtra(USER_ID, id);

        startActivity(intent);

        return true;
    }

    public void startEditProfileActivity(long id){

        if (colleted())
           return;

        if (editProfileActivity==null)
            return;

        Intent intent = new Intent(context.get(), editProfileActivity);
        intent.putExtra(USER_ID, id);

        startActivity(intent);
    }

    public SuperToast getToast(){
        if(toast == null) {
            toast = new SuperToast(context.get());
            toast.setDuration(SuperToast.Duration.MEDIUM);
            toast.setBackground(SuperToast.Background.BLUE);
            toast.setTextColor(Color.WHITE);
            toast.setAnimations(SuperToast.Animations.FLYIN);
        }
        return toast;
    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
    public void initCardToast(){

        if (colleted())
            return;

        if (context.get() instanceof AppCompatActivity)
        {
            if (superCardToastProgress == null) {

            }

        }
    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
//    public void dismissProgressCard(){
//        dismissProgressCard(0);
//    }

    /** You should pass Activity and not a context if you want to use this.*/
//    public void dismissProgressCardWithSmallDelay(){
//        dismissProgressCard(1500);
//    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
//    @Override
//    public void dismissProgressCard(long delay){
//        if (superCardToastProgress == null)
//            return;
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                superCardToastProgress.dismiss();
//            }
//        }, delay);
//    }
//
//    public void setProgress (float progress) {
//        if (superCardToastProgress != null) {
//            superCardToastProgress.setMaxProgress(1000);
//            superCardToastProgress.setProgress(Math.round(1000 * progress));
//        }
//    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
//    @Override
//    public void showProgressCard(String text){
//
//        if (colleted())
//            return;
//
////        if (context.get() instanceof AppCompatActivity) {
//
//            initCardToast();
//
//            View decorView = ((AppCompatActivity) context.get()).getWindow().getDecorView().findViewById(android.R.id.content);
//            ViewGroup viewGroup = superCardToastProgress.getViewGroup();
//
//            if (viewGroup!=null && superCardToastProgress.getView()!= null && viewGroup.findViewById(superCardToastProgress.getView().getId()) != null)
//                viewGroup.removeView(superCardToastProgress.getView());
//
//            decorView.findViewById(R.id.card_container).bringToFront();
//
//            superCardToastProgress.setText(text);
//
//            if (!superCardToastProgress.isShowing())
//                superCardToastProgress.show();
//
////        }
//    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
//    public void showProgressCard(@StringRes int resourceId){
//        showProgressCard(context.get().getString(resourceId));
//    }

    /*Getters and Setters*/
    public void showToast(String text){
        getAlertToast().setText(text);
        getAlertToast().show();
    }

    public void showToast(@StringRes int resourceId){
        if (context.get() == null)
            return;

        showToast(context.get().getString(resourceId));
    }

    public void setAlertToast(SuperToast alertToast) {
        this.alertToast = alertToast;
    }

    public void setToast(SuperToast toast) {
        this.toast = toast;
    }

    public SuperToast getAlertToast() {
        if(alertToast == null) {
            alertToast = new SuperToast(context.get());
            alertToast.setDuration(SuperToast.Duration.MEDIUM);
            alertToast.setBackground(SuperToast.Background.RED);
            alertToast.setTextColor(Color.WHITE);
            alertToast.setAnimations(SuperToast.Animations.FLYIN);
        }
        return alertToast;
    }

    public void setSearchActivity(Class searchActivity) {
        this.searchActivity = searchActivity;
    }

    public void setPickFriendsActivity(Class pickFriendsActivity) {
        this.pickFriendsActivity = pickFriendsActivity;
    }

    public void setShareWithFriendsActivity(Class shareWithFriendsActivity) {
        this.shareWithFriendsActivity = shareWithFriendsActivity;
    }

    public void setProfileActivity(Class profileActivity) {
        this.profileActivity = profileActivity;
    }

    @Deprecated
    private boolean colleted(){
        return context == null || context.get() == null;
        
    }

    private void startActivity(Intent intent){
        if (colleted())
            return;

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.get().startActivity(intent);
    }
    
    private void startActivity(Class activity){
        if (colleted())
        {
            return;
        }

        startActivity(new Intent(context.get(), activity));
    }
}


