package com.braunster.chatsdk.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.object.BError;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.Prefs.LoginTypeKey;

/*
 * Created by itzik on 6/8/2014.
 */


public class BFacebookManager {

    private static final String TAG = BFacebookManager.class.getSimpleName();
    private static final boolean DEBUG = Debug.BFacebookManager;

    public static String userFacebookID, userFacebookAccessToken, userFacebookName;
    private static String facebookAppID;
    private static String userThirdPartyUserAccount;

    private static SimpleLogin simpleLogin;

    public static void init(String id, Context context) {
       if (DEBUG) Log.i(TAG, "Initialized");
        Firebase ref = FirebasePaths.firebaseRef();
        simpleLogin = new SimpleLogin(ref);
        facebookAppID = id;
    }

    public static void loginWithFacebook(final CompletionListener completionListener) {
        if (DEBUG) Log.v(TAG, "loginWithFacebook");
        /*simpleLogin.loginWithFacebook(facebookAppID, userFacebookAccessToken, new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                if (error == null && user != null) {
                    String accessToken = (String) user.getThirdPartyUserData().get(AccessToken);

                    setNetworkCredentials(user.getUserId(), (String) user.getThirdPartyUserData().get(DISPLAY_NAME), accessToken);

                    completionListener.onDone();
                } else {
                    if (error != null)
                        Log.e(TAG, "Error: " + error.getMessage());

                    setNetworkCredentials(null, null, null);
                    completionListener.onDoneWithError();
                }

            }
        });*/

        BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(
                FirebasePaths.getMap(new String[]{BDefines.Keys.Facebook.AccessToken, LoginTypeKey}, userFacebookAccessToken, Facebook),
                    new CompletionListenerWithDataAndError<User, Object>() {
            @Override
            public void onDone(User firebaseSimpleLoginUser) {
                if (DEBUG) Log.i(TAG, "Logged to firebase");
                // Setting the user facebook id, This will be used to pull data on the user.(Friends list, profile pic etc...)
                userFacebookID = firebaseSimpleLoginUser.getUserId();
                completionListener.onDone();
            }

            @Override
            public void onDoneWithError(User fuser, Object o) {
                if (DEBUG) Log.e(TAG, "Log to firebase failed");
                completionListener.onDone();
            }
        });
    }

    /** Re authenticate after session state changed.*/
    public static void onSessionStateChange(Session session, SessionState state, Exception exception,final  CompletionListener completionListener) {
        if (DEBUG) Log.i(TAG, "Session changed state");

        if (exception != null)
        {
            exception.printStackTrace();
            if (exception instanceof FacebookOperationCanceledException)
            {
                if (DEBUG) Log.d(TAG, "Canceled");
                return;
            }
        }

        if (state.isOpened()) {
            if (DEBUG) Log.i(TAG, "Session is open.");

            // We will need this session later to make request.
            userFacebookAccessToken = Session.getActiveSession().getAccessToken();

            loginWithFacebook(new CompletionListener() {
                @Override
                public void onDone() {
                    completionListener.onDone();
                }

                @Override
                public void onDoneWithError() {
                    simpleLogin.logout();
                    completionListener.onDoneWithError();

                }
            });

        } else if (state.isClosed()) {
            // Logged out of Facebook
            if (DEBUG) Log.i(TAG, "Session is closed.");
            simpleLogin.logout();
            completionListener.onDoneWithError();
        }
        else
        {
//            completionListener.onDoneWithError();
            if (DEBUG) Log.i(TAG, "ELSE" + session.getApplicationId());
        }
    }

    public static boolean isAuthenticated() {
        return userFacebookID != null && userFacebookAccessToken != null;
    }

    public static String getUserFacebookID() {
        return userFacebookID;
    }

    public static void getUserDetails(final CompletionListenerWithData<GraphUser> listenerWithData){
        if (DEBUG) Log.v(TAG, "getUserDetails, Sessios State: " + Session.getActiveSession().getState().isOpened() + " isAuth: " + isAuthenticated());
        if (Session.getActiveSession().getState().isOpened())
        {
            // Request user data and show the results
            Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback()
            {
                @Override
                public void onCompleted(GraphUser user, Response response)
                {
                    if (response != null)
                    {
                        try
                        {
                            listenerWithData.onDone(user);
                        }
                        catch (Exception e)
                        {
                            listenerWithData.onDoneWithError(BError.getExceptionError(e));
                        }

                    }
                }
            }).executeAsync();
        } else listenerWithData.onDoneWithError(new BError(BError.Code.SESSION_CLOSED));
    }

    /*
    * No need for access token in SDK V3
    * Get the friend list from facebook that is using the app.*/
    public static void getUserFriendList(final CompletionListenerWithData completionListener){
        if (!Session.getActiveSession().getState().isOpened())
        {
            completionListener.onDoneWithError(new BError(BError.Code.SESSION_CLOSED));
            return;
        }
        Request req = Request.newMyFriendsRequest(Session.getActiveSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
//                if (DEBUG) Log.d(TAG, "Completed: " + response.getRawResponse());
                for (GraphUser u : users)
                    if (DEBUG) Log.d(TAG, "User Name: " + u.getName());

                completionListener.onDone(users);
            }
        });

        req.executeAsync();
    }

    /** Does not work if your app dosent have facebook game app privileges.*/
    public static void getInvitableFriendsList(final CompletionListenerWithData completionListenerWithData){

        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {

            // Get a list of friends who have _not installed_ the game.
            Request invitableFriendsRequest = Request.newGraphPathRequest(session,
                    "/me/invitable_friends", new Request.Callback() {

                        @Override
                        public void onCompleted(Response response) {

                            FacebookRequestError error = response.getError();
                            if (error != null) {
                                Log.e(TAG, error.toString());
                                completionListenerWithData.onDoneWithError(new BError(BError.Code.TAGGED, "Error while fetching invitable friends.", error));
                            } else if (session == Session.getActiveSession()) {
                                if (response != null) {
                                    // Get the result
                                    GraphObject graphObject = response.getGraphObject();
                                    JSONArray dataArray = (JSONArray)graphObject.getProperty("data");

                                    List<JSONObject> invitableFriends = new ArrayList<JSONObject>();
                                    if (dataArray.length() > 0) {
                                        // Ensure the user has at least one friend ...

                                        for (int i=0; i<dataArray.length(); i++) {
                                            invitableFriends.add(dataArray.optJSONObject(i));
                                        }
                                    }
                                    completionListenerWithData.onDone(invitableFriends);
                                }
                            }
                        }

                    });

            Bundle invitableParams = new Bundle();
            invitableParams.putString("fields", "id,first_name,picture");
            invitableFriendsRequest.setParameters(invitableParams);
            invitableFriendsRequest.executeAsync();
        }
        else
        {
            if (DEBUG) Log.d(TAG, "Session is closed");
            completionListenerWithData.onDoneWithError(new BError(BError.Code.SESSION_CLOSED));
        }

    }

    /*Helpers */
    public static String getPicUrl(String id){
        return "http://graph.facebook.com/"+id+"/picture?type=large";
    }
    public static String getPicUrl(String id, String type){
        return "http://graph.facebook.com/"+id+"/picture?type=" + type;
    }
    public static String getCurrentUserProfilePicURL(){
        return getPicUrl(facebookAppID);
    }
}




