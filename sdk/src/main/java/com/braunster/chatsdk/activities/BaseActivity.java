package com.braunster.chatsdk.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.UiUtils;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.concurrent.Callable;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends ActionBarActivity implements BaseActivityInterface{

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final boolean DEBUG = Debug.BaseActivity;

    public static final String FROM_LOGIN = "From_Login";

    UiLifecycleHelper uiHelper;

    private ProgressDialog progressDialog;

    /** If true the app will check if the user is online each time the activity is resumed.
     *  When the app is on the background the Android system can kill the app.
     *  When the app is killed the firebase api will set the user online status to false.
     *  Also all the listeners attached to the user, threads, friends etc.. are gone so each time the activity is resumed we will
     *  check to see if the user is online and if not start the auth process for registering to all events.*/
    private boolean checkOnlineOnResumed = false;

    /** If true the activity will implement facebook SDK components like sessionChangeState and the facebook UI helper.
     * This is good for caching a press on the logout button in the main activity or in any activity that will implement the facebook login button.*/
    private boolean integratedWithFacebook = false;

    /** A flag indicates that the activity in opened from the login activity so we wont do auth check when the activity will get to the onResume state.*/
    boolean fromLoginActivity = false;

    private boolean enableCardToast = false;

    SuperActivityToast superActivityToast;
    SuperToast superToast;
    SuperCardToast superCardToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (integratedWithFacebook)
        {
            uiHelper = new UiLifecycleHelper(this, callback);
            uiHelper.onCreate(savedInstanceState);
        }

        if (getIntent() != null && getIntent().getExtras() != null)
        {
            if (DEBUG) Log.d(TAG, "From login");
            fromLoginActivity = getIntent().getExtras().getBoolean(FROM_LOGIN, false);
            // So we wont encounter this flag again.
            getIntent().removeExtra(FROM_LOGIN);
        } else fromLoginActivity = false;

        if (savedInstanceState != null)
            fromLoginActivity = savedInstanceState.getBoolean(FROM_LOGIN, false);

        if (enableCardToast)
            SuperCardToast.onRestoreState(savedInstanceState, BaseActivity.this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getExtras() != null)
        {
            if (DEBUG) Log.d(TAG, "From login");
            fromLoginActivity = intent.getExtras().getBoolean(FROM_LOGIN, false);
            // So we wont encounter this flag again.
            intent.removeExtra(FROM_LOGIN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (integratedWithFacebook)
            uiHelper.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DEBUG) Log.v(TAG, "onResumed, From login: " + fromLoginActivity +", Check online: " + checkOnlineOnResumed);

        if (integratedWithFacebook)
            uiHelper.onResume();

        if (checkOnlineOnResumed && !fromLoginActivity)
        {
            if(DEBUG) Log.d(TAG, "Check online on resumed");
            BNetworkManager.sharedManager().getNetworkAdapter().isOnline(new CompletionListenerWithData<Boolean>() {
                @Override
                public void onDone(Boolean online) {
                    if (online == null) return;

                    if(DEBUG) Log.d(TAG, "Check done, " + online);

                    if (!online)
                    {
/*                        int loginTypeKey = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);
                        if ( loginTypeKey == Provider.FACEBOOK.ordinal()) {
                            if(DEBUG) Log.d(TAG, "FB AUTH");

                            // If the active session is null we will pull the session from cache.
                            Session session;
                            if (Session.getActiveSession() == null)
                            {
                                if (DEBUG) Log.e(TAG, "active session is null");
                                session = Session.openActiveSessionFromCache(BaseActivity.this);
                            }
                            else session = Session.getActiveSession();

                            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Keys.Facebook.AccessToken} , loginTypeKey, session.getAccessToken());

                            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(
                                    data, new CompletionListenerWithDataAndError<User, Object>() {
                                        @Override
                                        public void onDone(User firebaseSimpleLoginUser) {
                                            showToast("Auth with fb done");
                                            onAuthenticated();
                                        }

                                        @Override
                                        public void onDoneWithError(User firebaseSimpleLoginUser, Object o) {
                                            if (firebaseSimpleLoginUser == null)
                                                if (DEBUG) Log.d(TAG, "Fireuser is null");
                                            if (o instanceof com.firebase.simplelogin.enums.Error)
                                                if (DEBUG) Log.d(TAG, ((Error) o).name());

                                            Log.d(TAG, "Auth with fb done with error");
                                        }
                                    });
                        }
                        else{*/
                            if(DEBUG) Log.d(TAG, "OTHER AUTH");
                            authenticate(new AuthListener() {
                                @Override
                                public void onCheckDone(boolean isAuthenticated) {
                                    if (!isAuthenticated)
                                        startLoginActivity(true);
                                }

                                @Override
                                public void onLoginDone() {
                                    if (DEBUG) Log.d(TAG, "Authenticated!");
                                    onAuthenticated();
                                }

                                @Override
                                public void onLoginFailed(BError error) {
                                    if (DEBUG) Log.d(TAG, "Authenticated Failed!");
                                    startLoginActivity(true);
                                }
                            });
//                        }

                    }

                }

                @Override
                public void onDoneWithError(BError error) {
                    if (DEBUG) Log.d(TAG, "Check online failed!");
                    startLoginActivity(true);
                }
            });
        }
        fromLoginActivity = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (integratedWithFacebook)
            uiHelper.onDestroy();

        dismissProgDialog();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (integratedWithFacebook) uiHelper.onSaveInstanceState(outState);

        outState.putBoolean(FROM_LOGIN, fromLoginActivity);

        if (enableCardToast)
            SuperCardToast.onSaveState(outState);
    }

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.
     * http://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext*/
    public void setupTouchUIToDismissKeyboard(View view) {
        UiUtils.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(BaseActivity.this);
                return false;
            }
        });
    }

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        UiUtils.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideSoftKeyboard(BaseActivity.this);
                return false;
            }
        }, exceptIDs);
    }

    /** Hide the Soft Keyboard.*/
    public static void hideSoftKeyboard(Activity activity) {
        UiUtils.hideSoftKeyboard(activity);
    }

    void initToast(){
        superActivityToast = new SuperActivityToast(this);
        superActivityToast.setDuration(SuperToast.Duration.MEDIUM);
        superActivityToast.setBackground(SuperToast.Background.BLUE);
        superActivityToast.setTextColor(Color.WHITE);
        superActivityToast.setAnimations(SuperToast.Animations.FLYIN);

        superToast = new SuperToast(this);
        superToast.setDuration(SuperToast.Duration.MEDIUM);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.setAnimations(SuperToast.Animations.FLYIN);

        if (enableCardToast)
        {
           initCardToast();
        }
    }

    private void initCardToast(){
        superCardToast = new SuperCardToast(BaseActivity.this, SuperToast.Type.PROGRESS_HORIZONTAL);
        superCardToast.setIndeterminate(true);
        superCardToast.setBackground(SuperToast.Background.WHITE);
        superCardToast.setTextColor(Color.BLACK);
        superCardToast.setSwipeToDismiss(true);
    }

    /** Show a SuperToast with the given text. */
    void showToast(String text){
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setText(text);
        superToast.show();
    }

    void showAlertToast(String text){
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setText(text);
        superToast.show();
    }

    void showCard(String text){
        showCard(text, 0);
    }

    void showCard(String text, int progress){
//            initCardToast();

        // Making sure the card is on top of all other views.
        findViewById(R.id.card_container).bringToFront();

        if (superCardToast == null || !superCardToast.isShowing())
            initCardToast();

        superCardToast.setProgress(progress);
        superCardToast.setText(text);

        if (!superCardToast.isShowing())
            superCardToast.show();
    }

    void updateCard(String text, int progress){
        if (superCardToast == null)
            return;

        superCardToast.setText(text);
        superCardToast.setProgress(progress);
    }

    void dismissCard(){
        dismissCard(0);
    }

    void dismissCardWithSmallDelay(){
        dismissCard(1500);
    }

    void dismissCard(long delay){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                superCardToast.dismiss();
            }
        }, delay);
    }

    /** Authenticates the current user.*/
    public void authenticate(AuthListener listener){
        BNetworkManager.sharedManager().getNetworkAdapter().checkUserAuthenticatedWithCallback(listener);
    }

    /** Create a thread for given users and name, When thread and all users are all pushed to the server the chat activity for this thread will be open.*/
    void createAndOpenThreadWithUsers(String name, BUser...users){
        BNetworkManager.sharedManager().getNetworkAdapter().createThreadWithUsers(name, new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object>() {

            BThread thread = null;

            @Override
            public boolean onMainFinised(BThread bThread, Object o) {
                if (o != null)
                {
                    Toast.makeText(BaseActivity.this, "Failed to start chat.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (DEBUG) Log.d(TAG, "New thread is created.");

                thread = bThread;

                return false;
            }

            @Override
            public boolean onItem(BUser item) {
                return false;
            }

            @Override
            public void onDone() {
                Log.d(TAG, "On done.");

                dismissProgDialog();

                if (thread != null)
                {
                    Log.d(TAG, "Stating chat for thread.");
                    startChatActivityForID(thread.getId());
                }
                else if (DEBUG) Log.e(TAG, "thread added is null");
            }

            @Override
            public void onItemError(BUser user, Object o) {
                if (DEBUG) Log.d(TAG, "Failed to add user to thread, User name: " +user.getName());
            }
        }, users);
    }

    /** Start the chat activity for the given thread id.
     * @param id is the long value of local db id.*/
    void startChatActivityForID(long id){
        Intent intent = new Intent(BaseActivity.this, ChatActivity.class);
        intent.putExtra(ChatActivity.THREAD_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    void startLoginActivity(boolean loggedOut){
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        if (loggedOut) intent.putExtra(LoginActivity.FLAG_LOGGED_OUT, true);
        startActivity(intent);
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
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
            // For handling orientation changed.
            e.printStackTrace();
        }
    }

    protected void showAlertDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title if not null
        if (title != null && !title.equals(""))
            alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(alert)
                .setCancelable(false)
                .setPositiveButton(p, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (pos != null)
                            try {
                                pos.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(n, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        if (neg != null)
                            try {
                                neg.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    private void onSessionStateChange(Session session, final SessionState state, Exception exception){
        BFacebookManager.onSessionStateChange(session, state, exception, new CompletionListener() {
            @Override
            public void onDone() {
                if (DEBUG) Log.i(TAG, "onDone");
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "onDoneWithError");
                // Facebook session is closed so we need to disconnect from firebase.
                BNetworkManager.sharedManager().getNetworkAdapter().logout();
                startLoginActivity(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Log.v(TAG, "onActivityResult");
        if (integratedWithFacebook)
            uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    // Facebook Login stuff.
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (integratedWithFacebook)
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

    public void setEnableCardToast(boolean enableCardToast) {
        this.enableCardToast = enableCardToast;
    }


    @Override
    public void onAuthenticated() {

    }

}

interface BaseActivityInterface{
    /** This method is called after the app is resumed and check the online status of the user. */
    public void onAuthenticated();
}

