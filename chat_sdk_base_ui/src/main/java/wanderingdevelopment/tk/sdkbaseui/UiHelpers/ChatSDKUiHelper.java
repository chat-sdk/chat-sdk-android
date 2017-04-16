/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package wanderingdevelopment.tk.sdkbaseui.UiHelpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import wanderingdevelopment.tk.sdkbaseui.R;
import com.braunster.chatsdk.Utils.Debug;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tk.wanderingdevelopment.chatsdk.core.interfaces.UiLauncherInterface;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKAbstractChatActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKAbstractLoginActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKAbstractProfileActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKChatActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKEditProfileActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKLocationActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKLoginActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKMainActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKPickFriendsActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKSearchActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKShareWithContactsActivity;
import wanderingdevelopment.tk.sdkbaseui.ActivityTemplates.ChatSDKThreadDetailsActivity;

public class ChatSDKUiHelper implements UiLauncherInterface {

    public static final String TAG = ChatSDKUiHelper.class.getSimpleName();
    public static final boolean DEBUG = Debug.UiUtils;

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.*/
    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener) {
        setupTouchUIToDismissKeyboard(view, onTouchListener, -1);
    }

    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, Integer... exceptIDs) {

        List<Integer> ids = new ArrayList<Integer>();
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

    public static ChatSDKUiHelper instance;

    /** Instance only contains the classes that need to be used, Dont try to use toast from instance.*/
    public static ChatSDKUiHelper getInstance() {
        if (instance == null) throw new NullPointerException("You need to init ui helper before using the sdk, " +
                "You can use default init if you want to test the sdk or use default components." +
                "The use of this class is to make the BaseComponents of the SDK Ui adapt to your LoginActivity, MainActivity and ChatActivity. ");

        return instance;
    }

    private static final int NULL = -1991;

    /** If one of this toast are initialized it will be used across the app as the default toast.
     *  Each context can set it's own toast using the ui helper the sdk offers.
     *  If you use the ChatSDKBaseActivity you can set your toast in any stage in the context lifecycle.*/
    private static SuperToast customAlertToast = null, customToast = null;

    private SuperToast toast, alertToast;
    private SuperCardToast superCardToastProgress;

    private WeakReference<Context> context;
    private Class chatActivity, mainActivity, loginActivity,
            searchActivity = ChatSDKSearchActivity.class,
            pickFriendsActivity = ChatSDKPickFriendsActivity.class,
            shareWithFriendsActivity = ChatSDKShareWithContactsActivity.class,
            shareLocationActivity = ChatSDKLocationActivity.class,
            editProfileActivity= ChatSDKEditProfileActivity.class,
            profileActivity = null,
            threadDetailsActivity = ChatSDKThreadDetailsActivity.class;

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

    @Override
    public Class getSearchActivity() {
        return searchActivity;
    }

    public Class getShareLocationActivity() {
        return shareLocationActivity;
    }

    public static ChatSDKUiHelper initDefault(){
        instance = new ChatSDKUiHelper(ChatSDKChatActivity.class, ChatSDKMainActivity.class, ChatSDKLoginActivity.class);
        return getInstance();
    }

    public static ChatSDKUiHelper init(Class chatActivity, Class mainActivity, Class loginActivity) {
        instance = new ChatSDKUiHelper(chatActivity, mainActivity, loginActivity);
        return getInstance();
    }

    public ChatSDKUiHelper(Class chatActivity, Class mainActivity, Class loginActivity) {
        this.chatActivity = chatActivity;
        this.mainActivity = mainActivity;
        this.loginActivity = loginActivity;
    }

    public ChatSDKUiHelper(Context context, Class chatActivity, Class mainActivity, Class loginActivity) {
        this.chatActivity = chatActivity;
        this.mainActivity = mainActivity;
        this.loginActivity = loginActivity;
        this.context = new WeakReference<Context>(context);

        init();
    }

    public ChatSDKUiHelper(Context context, Class chatActivity, Class mainActivity, Class loginActivity, Class searchActivity, Class pickFriendsActivity, Class shareWithFriendsActivity, Class shareLocationActivity, Class profileActivity) {
        this.context = new WeakReference<Context>(context);
        this.chatActivity = chatActivity;
        this.mainActivity = mainActivity;
        this.loginActivity = loginActivity;
        this.searchActivity = searchActivity;
        this.pickFriendsActivity = pickFriendsActivity;
        this.shareWithFriendsActivity = shareWithFriendsActivity;
        this.shareLocationActivity = shareLocationActivity;
        this.profileActivity = profileActivity;

        init();
    }

    public ChatSDKUiHelper get(Context context){
        return new ChatSDKUiHelper(context, chatActivity, mainActivity, loginActivity, searchActivity, pickFriendsActivity, shareWithFriendsActivity, shareLocationActivity, profileActivity);
    }

    private void init(){
        if (customToast != null)
            setToast(customToast);
        else initDefaultToast();

        if (customAlertToast != null)
            setAlertToast(customAlertToast);
        else initDefaultAlertToast();
    }

    /** Start the chat activity for given thread id.*/
    @Override
    public void startChatActivityForID(long id){
        
        if (colleted())
            return;
        
        Intent intent = new Intent(context.get(), chatActivity);
        intent.putExtra(ChatSDKAbstractChatActivity.THREAD_ID, id);
        
        /**
         * Note
         *
         * * Not sure if do needed
         * 
         * There could be problems when this is activated but i am not sure if the activity should be recreated each time its called.
         * * * *
         **/
//        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        
        startActivity(intent);
    }

    @Override
    public void startLoginActivity(boolean loggedOut){
        Intent intent = new Intent(context.get(), loginActivity);
        intent.putExtra(ChatSDKAbstractLoginActivity.FLAG_LOGGED_OUT, loggedOut);
        startActivity(intent);
    }

    @Override
    public void startMainActivity(){
        startActivity(mainActivity);
    }

    @Override
    public void startSearchActivity(){
        startActivity(searchActivity);
    }

    @Override
    public void startPickFriendsActivity(){
        startActivity(pickFriendsActivity);
    }

    @Override
    public void startShareWithFriendsActivity(){
        if (colleted())
            return;

        startActivity(shareWithFriendsActivity);
    }

    @Override
    public void startShareLocationActivityActivity(){
        if (colleted())
            return;

        startActivity(shareLocationActivity);
    }

    @Override
    public boolean startProfileActivity(String entityId){

        if (colleted())
            return false;
        
        if (profileActivity==null)
            return false;

        Intent intent = new Intent(context.get(), profileActivity);
        intent.putExtra(ChatSDKAbstractProfileActivity.USER_ENTITY_ID, entityId);

        startActivity(intent);

        return true;
    }

    @Override
    public boolean startProfileActivity(long id){

        if (colleted())
            return false;

        if (profileActivity==null)
            return false;

        Intent intent = new Intent(context.get(), profileActivity);
        intent.putExtra(ChatSDKAbstractProfileActivity.USER_ID, id);

        startActivity(intent);

        return true;
    }

    @Override
    public void startEditProfileActivity(long id){

        if (colleted())
           return;

        if (editProfileActivity==null)
            return;

        Intent intent = new Intent(context.get(), editProfileActivity);
        intent.putExtra(ChatSDKAbstractProfileActivity.USER_ID, id);

        startActivity(intent);
    }

    
    
    
    public interface ChatSDKUiHelperInterface{
        /** Start the chat activity for given thread id.*/
        public void startChatActivityForID(long id);

        public void startLoginActivity(boolean loggedOut);

        public void startMainActivity();

        public void startSearchActivity();

        public void startPickFriendsActivity();

        public void startShareWithFriendsActivity();

        public void startShareLocationActivityActivity();
    }

    private void initDefaultAlertToast(){
        if (colleted())
            return;
        
        alertToast = new SuperToast(context.get());
        alertToast.setDuration(SuperToast.Duration.MEDIUM);
        alertToast.setBackground(SuperToast.Background.RED);
        alertToast.setTextColor(Color.WHITE);
        alertToast.setAnimations(SuperToast.Animations.FLYIN);

    }

    private void initDefaultToast(){
        if (colleted())
            return;
        
        toast = new SuperToast(context.get());
        toast.setDuration(SuperToast.Duration.MEDIUM);
        toast.setBackground(SuperToast.Background.BLUE);
        toast.setTextColor(Color.WHITE);
        toast.setAnimations(SuperToast.Animations.FLYIN);

    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
    @Override
    public void initCardToast(){

        if (colleted())
            return;
        
        if (context.get() instanceof AppCompatActivity)
        {
            if (superCardToastProgress != null)
                return;

            try {
                superCardToastProgress = new SuperCardToast((AppCompatActivity) context.get(), SuperToast.Type.PROGRESS);
                superCardToastProgress.setIndeterminate(true);
                superCardToastProgress.setBackground(SuperToast.Background.WHITE);
                superCardToastProgress.setTextColor(Color.BLACK);
                superCardToastProgress.setSwipeToDismiss(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
    @Override
    public void dismissProgressCard(){
        dismissProgressCard(0);
    }

    /** You should pass Activity and not a context if you want to use this.*/
    @Override
    public void dismissProgressCardWithSmallDelay(){
        dismissProgressCard(1500);
    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
    @Override
    public void dismissProgressCard(long delay){
        if (superCardToastProgress == null)
            return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                superCardToastProgress.dismiss();
            }
        }, delay);
    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
    @Override
    public void showProgressCard(String text){

        if (colleted())
            return;

        if (context.get() instanceof AppCompatActivity) {

            initCardToast();

            View decorView = ((AppCompatActivity) context.get()).getWindow().getDecorView().findViewById(android.R.id.content);
            ViewGroup viewGroup = superCardToastProgress.getViewGroup();

            if (viewGroup!=null && superCardToastProgress.getView()!= null && viewGroup.findViewById(superCardToastProgress.getView().getId()) != null)
                viewGroup.removeView(superCardToastProgress.getView());

            decorView.findViewById(R.id.card_container).bringToFront();

            superCardToastProgress.setText(text);

            if (!superCardToastProgress.isShowing())
                superCardToastProgress.show();

        }
    }

    /** You should pass שמ Activity and not a context if you want to use this.*/
    @Override
    public void showProgressCard(@StringRes int resourceId){
        showProgressCard(context.get().getString(resourceId));
    }

    /*Getters and Setters*/
    @Override
    public void showAlertToast(String text){
        alertToast.setText(text);
        alertToast.show();
    }

    @Override
    public void showAlertToast(@StringRes int resourceId){
        if (context.get() == null)
            return;
        
        showAlertToast(context.get().getString(resourceId));
    }

    @Override
    public void showToast(String text){
        toast.setText(text);
        toast.show();
    }

    @Override
    public void showToast(@StringRes int resourceId){
        if (context.get() == null)
            return;

        showToast(context.get().getString(resourceId));
    }

    @Override
    public void setAlertToast(SuperToast alertToast) {
        this.alertToast = alertToast;
    }

    @Override
    public void setToast(SuperToast toast) {
        this.toast = toast;
    }

    @Override
    public SuperToast getAlertToast() {
        return alertToast;
    }

    @Override
    public SuperToast getToast() {
        return toast;
    }

    public static void setCustomToast(SuperToast customToast) {
        ChatSDKUiHelper.customToast = customToast;
    }

    public static void setCustomAlertToast(SuperToast customAlertToast) {
        ChatSDKUiHelper.customAlertToast = customAlertToast;
    }

    @Override
    public void setSearchActivity(Class searchActivity) {
        this.searchActivity = searchActivity;
    }

    @Override
    public void setPickFriendsActivity(Class pickFriendsActivity) {
        this.pickFriendsActivity = pickFriendsActivity;
    }

    @Override
    public void setShareWithFriendsActivity(Class shareWithFriendsActivity) {
        this.shareWithFriendsActivity = shareWithFriendsActivity;
    }

    @Override
    public void setShareLocationActivity(Class shareLocationActivity) {
        this.shareLocationActivity = shareLocationActivity;
    }

    @Override
    public void setProfileActivity(Class profileActivity) {
        this.profileActivity = profileActivity;
    }
    
    private boolean colleted(){
        return context == null || context.get() == null;
        
    }

    private void startActivity(Intent intent){
        if (colleted())
            return;

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


