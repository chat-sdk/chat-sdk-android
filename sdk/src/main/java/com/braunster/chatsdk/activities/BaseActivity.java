package com.braunster.chatsdk.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Toast;

import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.firebase.simplelogin.User;
import com.firebase.simplelogin.enums.Error;
import com.firebase.simplelogin.enums.Provider;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.Map;

/**
 * Created by braunster on 18/06/14.
 */
public class BaseActivity extends ActionBarActivity implements BaseActivityInterface{

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

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
    private boolean fromLoginActivity = false;

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
            fromLoginActivity = getIntent().getExtras().getBoolean(FROM_LOGIN, false);
            // So we wont encounter this flag again.
            getIntent().getExtras().remove(FROM_LOGIN);
        }

        if (enableCardToast)
            SuperCardToast.onRestoreState(savedInstanceState, BaseActivity.this);

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
        if (integratedWithFacebook)
            uiHelper.onResume();

        if (checkOnlineOnResumed && !fromLoginActivity)
        {
            fromLoginActivity = false;

            if(DEBUG) Log.d(TAG, "Check online on resumed");
            BNetworkManager.sharedManager().getNetworkAdapter().isOnline(new CompletionListenerWithData<Boolean>() {
                @Override
                public void onDone(Boolean online) {
                    if (online == null) return;

                    if(DEBUG) Log.d(TAG, "Check done, " + online);
                    if (!online)
                    {
                        int loginTypeKey = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);
                        if ( loginTypeKey == Provider.FACEBOOK.ordinal()) {
                            if(DEBUG) Log.d(TAG, "FB AUTH");

                            // If the active session is null we will pull the session from cache.
                            Session session;
                            if (Session.getActiveSession() == null)
                            {
                                if (DEBUG) Log.e(TAG, "active session is null");
                                session = Session.openActiveSessionFromCache(BaseActivity.this);

                                /*session = Session.openActiveSession(BaseActivity.this, false, new Session.StatusCallback() {
                                    @Override
                                    public void call(Session session, SessionState state, Exception exception) {
                                        BFacebookManager.onSessionStateChange(session, state, exception, new CompletionListener() {
                                            @Override
                                            public void onDone() {

                                            }

                                            @Override
                                            public void onDoneWithError() {

                                            }
                                        });
                                    }
                                });
                                return;*/
                            }
                            else session = Session.getActiveSession();

                            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BFacebookManager.ACCESS_TOKEN} , loginTypeKey, session.getAccessToken());

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
                        else{
                            if(DEBUG) Log.d(TAG, "OTHER AUTH");
                            authenticate(new AuthListener() {
                                @Override
                                public void onCheckDone(boolean isAuthenticated) {

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
                        }

                    }

                }

                @Override
                public void onDoneWithError(BError error) {
                    if (DEBUG) Log.d(TAG, "Check online failed!");
                    startLoginActivity(true);
                }
            });
        }
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

        SuperCardToast.onSaveState(outState);
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
            superCardToast = new SuperCardToast(BaseActivity.this, SuperToast.Type.PROGRESS_HORIZONTAL);
            superCardToast.setIndeterminate(true);
            superCardToast.setBackground(SuperToast.Background.WHITE);
            superCardToast.setTextColor(Color.BLACK);
            superCardToast.setSwipeToDismiss(true);
        }
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
        if (superCardToast == null)
            initToast();

        superCardToast.setProgress(progress);
        superCardToast.setText(text);
        superCardToast.show();
    }

    void updateCard(String text, int progress){
        if (superCardToast == null)
            return;

        superCardToast.setText(text);
        superCardToast.setProgress(progress);
    }

    void dismissCard(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                superCardToast.dismiss();
            }
        }, 1500);
    }

/*    void showToast(int type, String text){
        superToast.setText(text);
        superToast.show();
    }*/

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
