/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import android.content.Context;
import android.os.Bundle;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.object.BError;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;

import org.jdeferred.Deferred;
import org.jdeferred.Promise;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

import static com.braunster.chatsdk.network.BDefines.BAccountType.Facebook;
import static com.braunster.chatsdk.network.BDefines.Prefs.LoginTypeKey;

/*
 * Created by itzik on 6/8/2014.
 */


public class BFacebookManager {

    private static final String TAG = BFacebookManager.class.getSimpleName();
    private static final boolean DEBUG = Debug.BFacebookManager;

    public static String userFacebookAccessToken;
    private static String facebookAppID;
    private static String userThirdPartyUserAccount;
    private static Context ctx;

    public static void init(String id, Context context) {
        facebookAppID = id;
        ctx = context;
    }

    public static Promise<Object, BError, Void> loginWithFacebook() {
        return BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(
                AbstractNetworkAdapter.getMap(new String[]{BDefines.Keys.ThirdPartyData.AccessToken, LoginTypeKey}, userFacebookAccessToken, Facebook));
    }

    /** Re authenticate after session state changed.*/
    public static Promise<Object, BError, Void> onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (DEBUG) Timber.i("Session changed state");

        // If we can start the login process with no errors this promise wont be used. 
        // The returned promise will be from the loginWithFacebook.
        Deferred<Object, BError, Void> deferred = new DeferredObject<>();
        
        if (exception != null)
        {
            exception.printStackTrace();
            if (exception instanceof FacebookOperationCanceledException)
            {
                deferred.reject(new BError(BError.Code.EXCEPTION, exception));
                return deferred.promise();
            }
        }

        if (state.isOpened()) {
            if (DEBUG) Timber.i("Session is open.");

            // We will need this session later to make request.
            userFacebookAccessToken = Session.getActiveSession().getAccessToken();

            return loginWithFacebook();

        } else if (state.isClosed()) {
            // Logged out of Facebook
            if (DEBUG) Timber.i("Session is closed.");
            deferred.reject(new BError(BError.Code.SESSION_CLOSED, "Facebook session is closed."));
        }
        
        
        return deferred.promise();
    }

    public static boolean isAuthenticated() {
        return  userFacebookAccessToken != null;
    }

    public static Promise<GraphObject, BError, Void> getUserDetails(){

        final Deferred<GraphObject, BError, Void> deferred = new DeferredObject<>();
        
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
                            deferred.resolve(user);
                        }
                        catch (Exception e)
                        {
                            deferred.reject(BError.getExceptionError(e));
                        }

                    }
                }
            }).executeAsync();
        } else deferred.reject(new BError(BError.Code.SESSION_CLOSED));
        
        return deferred.promise();
    }

    /*
    * No need for access token in SDK V3
    * Get the friend list from facebook that is using the app.*/
    public static  Promise<List<GraphUser>, BError, Void>  getUserFriendList(){

        final Deferred<List<GraphUser>, BError, Void> deferred = new DeferredObject<>();

        
        if (!Session.getActiveSession().getState().isOpened())
        {
            return deferred.reject(new BError(BError.Code.SESSION_CLOSED));
        }
        Request req = Request.newMyFriendsRequest(Session.getActiveSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                deferred.resolve(users);
            }
        });

        req.executeAsync();
        
        return deferred.promise();
    }

    /** Does not work if your app dosent have facebook game app privileges.*/
    public static Promise<List<JSONObject>, BError, Void> getInvitableFriendsList(){

        final Deferred<List<JSONObject>, BError, Void> deferred = new DeferredObject<>();
        
        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {

            // Get a list of friends who have _not installed_ the game.
            Request invitableFriendsRequest = Request.newGraphPathRequest(session,
                    "/me/invitable_friends", new Request.Callback() {

                        @Override
                        public void onCompleted(Response response) {

                            FacebookRequestError error = response.getError();
                            if (error != null) {
                                if (DEBUG) Timber.e(error.toString());
                                deferred.reject(new BError(BError.Code.TAGGED, "Error while fetching invitable friends.", error));
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
                                    deferred.resolve(invitableFriends);
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
            if (DEBUG) Timber.d("Session is closed");
            deferred.reject(new BError(BError.Code.SESSION_CLOSED));
        }
        
        return deferred.promise();

    }

    /*Helpers */
    public static String getPicUrl(String id){
        return "http://graph.facebook.com/"+id+"/picture?type=large";
    }
    public static String getPicUrl(String id, String type){
        return "http://graph.facebook.com/"+id+"/picture?type=" + type;
    }

    // For making sure that we logout completely from facebook.
    public static void logout(Context ctx){
        userFacebookAccessToken = null;

        if (Session.getActiveSession() != null)
        {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
        else
        {
            if (DEBUG) Timber.e("getActiveSessionIsNull");
            Session session = Session.openActiveSessionFromCache(ctx);

            if (session != null)
                session.closeAndClearTokenInformation();
        }
    }

}




