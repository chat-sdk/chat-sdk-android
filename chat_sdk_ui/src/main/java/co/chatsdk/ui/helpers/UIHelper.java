/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.R;
import co.chatsdk.ui.activities.SelectContactActivity;
import co.chatsdk.ui.profile.EditProfileActivity;
import co.chatsdk.ui.profile.EditProfileActivity2;
import co.chatsdk.ui.activities.ShareWithContactsActivity;
import co.chatsdk.ui.profile.ProfileActivity;
import co.chatsdk.ui.threads.ThreadDetailsActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.chatsdk.ui.chat.ChatActivity;
import co.chatsdk.ui.login.AbstractLoginActivity;
import co.chatsdk.ui.login.LoginActivity;
import co.chatsdk.ui.activities.MainActivity;
import co.chatsdk.ui.activities.SearchActivity;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

public class UIHelper {

    public static final String TAG = UIHelper.class.getSimpleName();

    public static String USER_ENTITY_ID = "USER_ENTITY_ID";

    private WeakReference<Context> context;

    private Class chatActivity = ChatActivity.class;
    private Class mainActivity = MainActivity.class;
    private Class loginActivity = LoginActivity.class;
    private Class searchActivity = SearchActivity.class;
    private Class pickFriendsActivity = SelectContactActivity.class;
    private Class shareWithFriendsActivity = ShareWithContactsActivity.class;
    private Class editProfileActivity2 = EditProfileActivity2.class;
    private Class editProfileActivity = EditProfileActivity.class;
    private Class profileActivity = ProfileActivity.class;
    private Class threadDetailsActivity = ThreadDetailsActivity.class;

    private final static UIHelper instance = new UIHelper();

    protected UIHelper () {
    }

    public static UIHelper shared() {
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
        Intent intent = new Intent(context.get(), chatActivity);
        intent.putExtra(ChatActivity.THREAD_ID, id);

        startActivity(intent);
    }

    public void startLoginActivity(boolean loggedOut){
        Intent intent = new Intent(context.get(), loginActivity);
        intent.putExtra(AbstractLoginActivity.FLAG_LOGGED_OUT, loggedOut);
        startActivity(intent);
    }

    public void startEditProfileActivity(boolean loggedOut, User user){
        Intent intent = new Intent(context.get(), editProfileActivity);
//        intent.putExtra(AbstractLoginActivity.FLAG_LOGGED_OUT, loggedOut);
        intent.putExtra(USER_ENTITY_ID, user.getEntityID());
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
        startActivity(shareWithFriendsActivity);
    }

    public boolean startProfileActivity(String entityId){

        if (profileActivity==null)
            return false;

        Intent intent = new Intent(context.get(), profileActivity);
        intent.putExtra(USER_ENTITY_ID, entityId);

        startActivity(intent);

        return true;
    }

    public void showToast(String text){
        Toast.makeText(context.get(), text, Toast.LENGTH_SHORT).show();
    }

    public void showToast(@StringRes int resourceId){
        if (context.get() == null)
            return;

        showToast(context.get().getString(resourceId));
    }

    private void startActivity(Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.get().startActivity(intent);
    }
    
    private void startActivity(Class activity){
        startActivity(new Intent(context.get(), activity));
    }

    /** Create or fetch chat for users, Opens the chat when done.*/
    public Single<Thread> createAndOpenThreadWithUsers(final Context context, String name, List<User> users) {
        return NM.thread().createThread(name, users).doOnSuccess(new Consumer<Thread>() {
            @Override
            public void accept(Thread thread) throws Exception {
                if (thread != null) {
                    startChatActivityForID(thread.getId());
                }
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                showToast(context.getString(R.string.create_thread_with_users_fail_toast));
            }
        });
    }

    public Single<Thread> createAndOpenThreadWithUsers(final Context context, String name, User...users){
        return createAndOpenThreadWithUsers(context, name, Arrays.asList(users));
    }

}


