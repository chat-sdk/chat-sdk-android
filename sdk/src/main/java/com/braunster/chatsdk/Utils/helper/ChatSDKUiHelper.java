package com.braunster.chatsdk.Utils.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.ChatSDKChatActivity;
import com.braunster.chatsdk.activities.ChatSDKLocationActivity;
import com.braunster.chatsdk.activities.ChatSDKLoginActivity;
import com.braunster.chatsdk.activities.ChatSDKMainActivity;
import com.braunster.chatsdk.activities.ChatSDKPickFriendsActivity;
import com.braunster.chatsdk.activities.ChatSDKSearchActivity;
import com.braunster.chatsdk.activities.ChatSDKShareWithContactsActivity;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractLoginActivity;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractProfileActivity;
import com.firebase.simplelogin.enums.FirebaseSimpleLoginErrorCode;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by braunster on 11/08/14.
 */
public class ChatSDKUiHelper {

    public static final String TAG = ChatSDKUiHelper.class.getSimpleName();
    public static final boolean DEBUG = Debug.UiUtils;

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.*/
    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener) {
        setupTouchUIToDismissKeyboard(view, onTouchListener, -1);
    }

    public static void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, Integer... exceptIDs) {
        if (DEBUG) Log.v(TAG, "setupTouchUIToDismissKeyboard");
        List<Integer> ids = new ArrayList<Integer>();
        if (exceptIDs != null)
            ids = Arrays.asList(exceptIDs);

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            if (!ids.isEmpty() && ids.contains(view.getId()))
            {
                if (DEBUG) Log.d(TAG, "Skipping View, ID: " + view.getId());
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
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

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
     *  Each activity can set it's own toast using the ui helper the sdk offers.
     *  If you use the ChatSDKBaseActivity you can set your toast in any stage in the activity lifecycle.*/
    private static SuperToast customAlertToast = null, customToast = null;

    private SuperToast toast, alertToast;
    private SuperCardToast superCardToastProgress;

    private Activity activity;
    public Class chatActivity, mainActivity, loginActivity,
            searchActivity = ChatSDKSearchActivity.class,
            pickFriendsActivity = ChatSDKPickFriendsActivity.class,
            shareWithFriendsActivity = ChatSDKShareWithContactsActivity.class,
            shareLocationActivity = ChatSDKLocationActivity.class,
            profileActivity = null;

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

    public ChatSDKUiHelper(Activity activity, Class chatActivity, Class mainActivity, Class loginActivity) {
        this.chatActivity = chatActivity;
        this.mainActivity = mainActivity;
        this.loginActivity = loginActivity;
        this.activity = activity;

        init();
    }

    public ChatSDKUiHelper(Activity activity, Class chatActivity, Class mainActivity, Class loginActivity, Class searchActivity, Class pickFriendsActivity, Class shareWithFriendsActivity, Class shareLocationActivity, Class profileActivity) {
        this.activity = activity;
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

    public ChatSDKUiHelper get(Activity activity){
        return new ChatSDKUiHelper(activity, chatActivity, mainActivity, loginActivity, searchActivity, pickFriendsActivity, shareWithFriendsActivity, shareLocationActivity, profileActivity);
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
    public void startChatActivityForID(long id){
        Intent intent = new Intent(activity, chatActivity);
        intent.putExtra(ChatSDKAbstractChatActivity.THREAD_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        activity.startActivity(intent);
    }

    public void startLoginActivity(boolean loggedOut){
        Intent intent = new Intent(activity, loginActivity);
        intent.putExtra(ChatSDKAbstractLoginActivity.FLAG_LOGGED_OUT, loggedOut);
        activity.startActivity(intent);
    }

    public void startMainActivity(){
        Intent intent = new Intent(activity, mainActivity);
        activity.startActivity(intent);
    }

    public void startSearchActivity(){
        Intent intent = new Intent(activity, searchActivity);
        activity.startActivity(intent);
    }

    public void startPickFriendsActivity(){
        Intent intent = new Intent(activity, pickFriendsActivity);
        activity.startActivity(intent);
    }

    public void startShareWithFriendsActivity(){
        Intent intent = new Intent(activity, shareWithFriendsActivity);
        activity.startActivity(intent);
    }

    public void startShareLocationActivityActivity(){
        Intent intent = new Intent(activity, shareLocationActivity);
        activity.startActivity(intent);
    }

    public boolean startProfileActivity(String entityId){
        if (profileActivity==null)
            return false;

        Intent intent = new Intent(activity, profileActivity);
        intent.putExtra(ChatSDKAbstractProfileActivity.USER_ENTITY_ID, entityId);

        activity.startActivity(intent);

        return true;
    }

    public boolean startProfileActivity(long id){
        if (profileActivity==null)
            return false;

        Intent intent = new Intent(activity, profileActivity);
        intent.putExtra(ChatSDKAbstractProfileActivity.USER_ID, id);

        activity.startActivity(intent);

        return true;
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
        alertToast = new SuperToast(activity);
        alertToast.setDuration(SuperToast.Duration.MEDIUM);
        alertToast.setBackground(SuperToast.Background.RED);
        alertToast.setTextColor(Color.WHITE);
        alertToast.setAnimations(SuperToast.Animations.FLYIN);

    }

    private void initDefaultToast(){
        toast = new SuperToast(activity);
        toast.setDuration(SuperToast.Duration.MEDIUM);
        toast.setBackground(SuperToast.Background.BLUE);
        toast.setTextColor(Color.WHITE);
        toast.setAnimations(SuperToast.Animations.FLYIN);

    }

    public void initCardToast(){
        if (superCardToastProgress != null)
            return;

        try {
            superCardToastProgress = new SuperCardToast(activity, SuperToast.Type.PROGRESS);
            superCardToastProgress.setIndeterminate(true);
            superCardToastProgress.setBackground(SuperToast.Background.WHITE);
            superCardToastProgress.setTextColor(Color.BLACK);
            superCardToastProgress.setSwipeToDismiss(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressCard(){
        dismissProgressCard(0);
    }

    public void dismissProgressCardWithSmallDelay(){
        dismissProgressCard(1500);
    }

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

    public void showProgressCard(String text){

        initCardToast();

        activity.getWindow().getDecorView().findViewById(android.R.id.content).findViewById(R.id.card_container).bringToFront();

        superCardToastProgress.setText(text);

        if (!superCardToastProgress.isShowing())
            superCardToastProgress.show();
    }

    /*Getters and Setters*/
    public void showAlertToast(String text){
        alertToast.setText(text);
        alertToast.show();
    }

    public void showToast(String text){
        toast.setText(text);
        toast.show();
    }

    public void setAlertToast(SuperToast alertToast) {
        this.alertToast = alertToast;
    }

    public void showSimpleLoginErroToast(FirebaseSimpleLoginErrorCode error){
        String toastMessage = "";

        switch (error)
        {
            case EmailTaken:
                toastMessage = "Email is taken.";
                break;

            case InvalidEmail:
                toastMessage = "Invalid Email.";
                break;

            case InvalidPassword:
                toastMessage = "Invalid Password";
                break;

            case AccountNotFound:
                toastMessage = "Account not found.";
                break;

            case AccessNotGranted:
                toastMessage = "Access not granted.";
                break;

            case OperationFailed:
                toastMessage = "Operation Failed";
                break;

            case UserDoesNotExist:
                toastMessage = "User does not exist";
                break;

            case PermissionDenied:
                toastMessage = "Permission denied";
                break;

            default: toastMessage = "An Error Occurred.";
        }

        showAlertToast(toastMessage);
    }

    public void setToast(SuperToast toast) {
        this.toast = toast;
    }

    public SuperToast getAlertToast() {
        return alertToast;
    }

    public SuperToast getToast() {
        return toast;
    }

    public static void setCustomToast(SuperToast customToast) {
        ChatSDKUiHelper.customToast = customToast;
    }

    public static void setCustomAlertToast(SuperToast customAlertToast) {
        ChatSDKUiHelper.customAlertToast = customAlertToast;
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

    public void setShareLocationActivity(Class shareLocationActivity) {
        this.shareLocationActivity = shareLocationActivity;
    }

    public void setProfileActivity(Class profileActivity) {
        this.profileActivity = profileActivity;
    }
}


