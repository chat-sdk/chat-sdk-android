package com.braunster.chatsdk.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.Map;

/**
 * Created by braunster on 18/06/14.
 */
public abstract class BaseActivity extends ActionBarActivity{

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final boolean DEBUG = true;

    UiLifecycleHelper uiHelper;
    private ProgressDialog progressDialog;
    private boolean checkOnlineOnResumed = false, integratedWithFacebook = false;
    SuperActivityToast superActivityToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (integratedWithFacebook)
        {
            uiHelper = new UiLifecycleHelper(this, callback);
            uiHelper.onCreate(savedInstanceState);
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
        if (integratedWithFacebook)
            uiHelper.onResume();

        if (checkOnlineOnResumed)
        {
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
                            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BFacebookManager.ACCESS_TOKEN} , loginTypeKey, Session.getActiveSession().getAccessToken());

                            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(
                                    data, new CompletionListenerWithDataAndError<User, Object>() {
                                        @Override
                                        public void onDone(User firebaseSimpleLoginUser) {
                                            showToast("Auth with fb done");
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
    }



    void initToast(){
        superActivityToast = new SuperActivityToast(this);
        superActivityToast.setDuration(SuperToast.Duration.MEDIUM);
        superActivityToast.setBackground(SuperToast.Background.BLUE);
        superActivityToast.setTextColor(Color.WHITE);
        superActivityToast.setAnimations(SuperToast.Animations.FLYIN);
        superActivityToast.setTouchToDismiss(true);
    }

    /** Show a SuperToast with the given text. */
    void showToast(String text){
        superActivityToast.setText(text);
        superActivityToast.show();
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
}
