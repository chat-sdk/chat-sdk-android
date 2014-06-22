package com.braunster.chatsdk.network;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.firebase.client.Firebase;
import com.firebase.simplelogin.FirebaseSimpleLoginError;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.SimpleLogin;
import com.firebase.simplelogin.SimpleLoginAuthenticatedHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */

public class BFacebookManager {

    private static final String TAG = BFacebookManager.class.getSimpleName();
    private static final boolean DEBUG = true;


    /**
     * User date that need to be stored.
     */

    public static String userFacebookID, userFacebookAccessToken, userFacebookName;
    private static String facebookAppID;/* Need to get it from a differnt source*/

    private static String userThirdPartyUserAccount; /* Not sure if needed*/


    /**
     * For login to facebook using firebase API
     */

    private static SimpleLogin simpleLogin;

    private static Session fbSession;

    public static void init(String id, Context context) {
       if (DEBUG) Log.i(TAG, "Initialized");
        Firebase ref = FirebasePaths.firebaseRef();
        simpleLogin = new SimpleLogin(ref);
        facebookAppID = id;
    }

    public static void loginWithFacebook(final CompletionListener completionListener) {
        simpleLogin.loginWithFacebook(facebookAppID, userFacebookAccessToken, new SimpleLoginAuthenticatedHandler() {
            @Override
            public void authenticated(FirebaseSimpleLoginError error, FirebaseSimpleLoginUser user) {
                if (error == null && user != null) {
                    String accessToken = (String) user.getThirdPartyUserData().get("accessToken");

                    setNetworkCredentials(user.getUserId(), (String) user.getThirdPartyUserData().get("displayName"), accessToken);

                    completionListener.onDone();
                } else {
                    if (error != null)
                        Log.e(TAG, "Error: " + error.getMessage());

                    setNetworkCredentials(null, null, null);
                    completionListener.onDoneWithError();
                }

            }
        });
    }

    public static void logoutFromFacebook() {
        if (simpleLogin != null)
            simpleLogin.logout();
        else if (DEBUG) Log.e(TAG, "Trying to logout but simple login is null");
    }

    /**
     * Re authenticate after session state changed.
     */
    public static void onSessionStateChange(Session session, SessionState state, Exception exception,final  CompletionListener completionListener) {
        if (DEBUG) Log.i(TAG, "Session changed state");

        if (exception != null)
            exception.printStackTrace();

        if (state.isOpened()) {
            if (DEBUG) Log.i(TAG, "Session is open.");

            // We will need this session later to make request.
            fbSession = session.getActiveSession();
            userFacebookAccessToken = fbSession.getAccessToken();

            loginWithFacebook(new CompletionListener() {
                @Override
                public void onDone() {
                    if (DEBUG) Log.i(TAG, "Logged to facebook");
                    completionListener.onDone();
                }

                @Override
                public void onDoneWithError() {
                    simpleLogin.logout();
                    if (DEBUG) Log.i(TAG, "Error while login.");
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

    private static void setNetworkCredentials(String facebookID, String name, String accessToken) {
        userFacebookID = facebookID;
        userFacebookName = name;
        userFacebookAccessToken = accessToken;
    }

    public static boolean isAuthenticated() {
        return userFacebookID != null && userFacebookAccessToken != null;
    }

    public static String getUserFacebookID() {
        return userFacebookID;
    }

    public static void getUserDetails(final CompletionListenerWithData<GraphUser> listenerWithData){
        if (fbSession != null && fbSession.getState().isOpened() && isAuthenticated())
        {
            // Request user data and show the results
            Request.newMeRequest(fbSession, new Request.GraphUserCallback()
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
                            e.printStackTrace();
                            listenerWithData.onDoneWithError();
                        }

                    }
                }
            }).executeAsync();
        }
    }

    public static void getProfilePictureForFacebookID(String facebookId, CompletionListener completionListener){
        // ASK if needed to implement this as facebook giving the ProfilePictureView
    }

    /** No need for access token in SDK V3*/
    /** Get the friend list from facebook that is using the app.*/
    public static void getUserFriendList(final CompletionListenerWithData completionListener){
        Request req = Request.newMyFriendsRequest(fbSession, new Request.GraphUserListCallback() {
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
                                completionListenerWithData.onDoneWithError();
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
            completionListenerWithData.onDoneWithError();
        }

    }

    /* Helpers */
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

/*ASK  Authenticate, Login, Logout, getFacebook data: profile pic and friends list. Save credentials for user.*/

/*
    @property (nonatomic, readwrite) ACAccount * userThirdPartyUserAccount; // Seems not relevant to android.

    @synthesize networkAdapter;
    */


