/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:32 PM
 */

package wanderingdevelopment.tk.sdkbaseui.Activities;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.types.AccountType;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.DialogUtils;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.ChatSDKUiHelper;
import com.braunster.chatsdk.network.BFacebookManager;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

import timber.log.Timber;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.BaseActivity;

    public static final String FROM_LOGIN = "From_Login";

    private UiLifecycleHelper uiHelper;

    private ProgressDialog progressDialog;

    /** If true the app will check if the user is online each time the activity is resumed.
     *  When the app is on the background the Android system can kill the app.
     *  When the app is killed the firebase api will set the user online status to false.
     *  Also all the listeners attached to the user, threads, friends etc.. are gone so each time the activity is resumed we will
     *  check to see if the user is online and if not start the auth process for registering to all events.*/
    private boolean checkOnlineOnResumed = false;

    /** If true the activity will implement facebook SDK components like sessionChangeState and the facebook UI helper.
     * This is good for caching a press on the logout button in the main activity or in any activity that will implement the facebook login button.*/
    protected boolean integratedWithFacebook = false;

    /** A flag indicates that the activity in opened from the login activity so we wont do auth check when the activity will get to the onResume state.*/
    boolean fromLoginActivity = false;

    /** Need to be set before on create, If true card toast will be available while activity run, You need to add a layout with a special id for this to work.
     * Example can be seen in the chat activity.
     * You cant use the card toast until onResume is called due to the config of the card toast. If you need it you can call initCardToast.*/
    private boolean enableCardToast = false;

    protected ChatSDKUiHelper chatSDKUiHelper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatSDKUiHelper = ChatSDKUiHelper.getInstance().get(this);

        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
        {
            uiHelper = new UiLifecycleHelper(this, callback);
            uiHelper.onCreate(savedInstanceState);
        }

        if (getIntent() != null && getIntent().getExtras() != null)
        {
            if (DEBUG) Timber.d("From login");
            fromLoginActivity = getIntent().getExtras().getBoolean(FROM_LOGIN, false);
            // So we wont encounter this flag again.
            getIntent().removeExtra(FROM_LOGIN);
        } else fromLoginActivity = false;

        if (savedInstanceState != null)
            fromLoginActivity = savedInstanceState.getBoolean(FROM_LOGIN, false);

        if (enableCardToast)
        {
            SuperCardToast.onRestoreState(savedInstanceState, BaseActivity.this);
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
        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
            uiHelper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (enableCardToast)
            chatSDKUiHelper.initCardToast();

        if (DEBUG) Timber.v("onResumed, From login: %s, Check online: %s", fromLoginActivity, checkOnlineOnResumed);

        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
            uiHelper.onResume();

        if (checkOnlineOnResumed && !fromLoginActivity)
        {
            if(DEBUG) Timber.d("Check online on resumed");
            getWindow().getDecorView().post(new Runnable() {
                @Override
                public void run() {
                    NM.core().isOnline().subscribe(new BiConsumer<Boolean, Throwable>() {
                        @Override
                        public void accept(Boolean online, Throwable throwable) throws Exception {
                            if(throwable == null) {
                                if(DEBUG) Timber.d("Check done, ", online);

                                if (!online)
                                {
                                    authenticate().subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                        }

                                        @Override
                                        public void onComplete() {
                                            if (DEBUG) Timber.d("Authenticated!");
                                            onAuthenticated();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            if (DEBUG) Timber.d("Authenticated Failed!");
                                            onAuthenticationFailed();
                                        }
                                    });

                                }
                            }
                        }
                    });

                }
            });
        }

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

        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
            uiHelper.onDestroy();

        dismissProgDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook)) uiHelper.onSaveInstanceState(outState);

        outState.putBoolean(FROM_LOGIN, fromLoginActivity);

        if (enableCardToast)
            SuperCardToast.onSaveState(outState);
    }

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.
     * http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext*/
    public void setupTouchUIToDismissKeyboard(View view) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(BaseActivity.this);
                return false;
            }
        });
    }

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(BaseActivity.this);
                return false;
            }
        }, exceptIDs);
    }

    public void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, final Integer... exceptIDs) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, onTouchListener, exceptIDs);
    }

    /** Hide the Soft Keyboard.*/
    public static void hideSoftKeyboard(AppCompatActivity activity) {
        ChatSDKUiHelper.hideSoftKeyboard(activity);
    }

    /** Show a SuperToast with the given text. */
    protected void showToast(String text){
        if (chatSDKUiHelper==null || StringUtils.isEmpty(text))
            return;
        chatSDKUiHelper.getToast().setText(text);
        chatSDKUiHelper.getToast().show();
    }

    protected void showProgressCard(String text){
        chatSDKUiHelper.showProgressCard(text);
    }

    protected void dismissProgressCard(){
        dismissProgressCard(0);
    }

    protected void dismissProgressCardWithSmallDelay(){
        dismissProgressCard(1500);
    }

    protected void dismissProgressCard(long delay){
        chatSDKUiHelper.dismissProgressCard(delay);
    }

    /** Authenticates the current user.*/
    public Completable authenticate(){
        return NM.auth().authenticateWithCachedToken();
    }

    /** Create a thread for given users and name, When thread and all users are all pushed to the server the chat activity for this thread will be open.*/
    protected void createAndOpenThreadWithUsers(String name, BUser...users){
        NM.thread().createThread(name, users).subscribe(new BiConsumer<BThread, Throwable>() {
            @Override
            public void accept(final BThread thread, Throwable throwable) throws Exception {
                if(throwable == null) {
                    dismissProgDialog();

                    if (thread == null) {
                        if (DEBUG) Timber.e("thread added is null");
                        return;
                    }

                    if (isOnMainThread()) {
                        startChatActivityForID(thread.getId());
                    } else BaseActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startChatActivityForID(thread.getId());
                        }
                    });
                }
                else {
                    if (isOnMainThread())
                        showToast(getString(R.string.create_thread_with_users_fail_toast));
                    else BaseActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showToast(getString(R.string.create_thread_with_users_fail_toast));
                        }
                    });
                }
            }
        });
    }

    /** Start the chat activity for the given thread id.
     * @param id is the long value of local db id.*/
    public void startChatActivityForID(long id){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startChatActivityForID(id);
    }

    public void startLoginActivity(boolean loggedOut){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startLoginActivity(loggedOut);
    }

    public void startMainActivity(){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startMainActivity();
    }

    public void startSearchActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startSearchActivity();
    }

    public void startPickFriendsActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startPickFriendsActivity();
    }

    public void startShareWithFriendsActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startShareWithFriendsActivity();
    }

    public void startShareLocationActivityActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startShareLocationActivityActivity();
    }

    protected void showProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void showOrUpdateProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.show();
        } else progressDialog.setMessage(message);
    }

    protected void dismissProgDialog(){
        // For handling orientation changed.
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
        }
    }

    protected void showToastDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
        DialogUtils.showToastDialog(this, title, alert, p, n, neg, pos);
    }

    protected void onSessionStateChange(Session session, final SessionState state, Exception exception){
        BFacebookManager.onSessionStateChange(session, state, exception).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (DEBUG) Timber.e("onDoneWithError. Error: %s", throwable.getMessage());
                // Facebook session is closed so we need to disconnect from firebase.
                NM.auth().logout();
                startLoginActivity(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Timber.v("onActivityResult");
        
        if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
            uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    // Facebook Login stuff.
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (integratedWithFacebook && NM.auth().accountTypeEnabled(AccountType.Facebook))
                onSessionStateChange(session, state, exception);
        }
    };

    /** When enabled the app will check the user online ref to see if he is not offline each time that the activity is resumed.
     *  This method is good for times that the app is in the background and killed by the android system and we need to listen to all the user details again.
     *  i.e Authenticate again.*/
    public void enableCheckOnlineOnResumed(boolean checkOnlineOnResumed) {
        this.checkOnlineOnResumed = checkOnlineOnResumed;
    }

    public void enableFacebookIntegration(boolean integratedWithFacebook) {
        this.integratedWithFacebook = integratedWithFacebook;
    }

    public void setCardToastEnabled(boolean enableCardToast) {
        this.enableCardToast = enableCardToast;
    }

    public void onAuthenticated() {

    }

    public void onAuthenticationFailed() {
        startLoginActivity(true);
    }

    protected boolean isOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return false;
        }

        return true;
    }

    /*Getters and Setters*/
    public void setAlertToast(SuperToast alertToast) {
        chatSDKUiHelper.setAlertToast(alertToast);
    }

    public void setChatSDKUiHelper(ChatSDKUiHelper chatSDKUiHelper) {
        this.chatSDKUiHelper = chatSDKUiHelper;
    }

    public void setToast(SuperToast toast) {
        chatSDKUiHelper.setToast(toast);
    }

    public SuperToast getToast() {
        return chatSDKUiHelper.getToast();
    }

    public SuperToast getAlertToast() {
        return chatSDKUiHelper.getAlertToast();
    }
}


