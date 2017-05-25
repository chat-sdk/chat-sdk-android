/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import android.content.Context;
import android.os.Bundle;

import co.chatsdk.core.dao.core.DaoDefines;
import co.chatsdk.core.defines.Debug;
import com.braunster.chatsdk.object.ChatError;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.Defines;
import io.reactivex.Completable;
import timber.log.Timber;

import co.chatsdk.core.types.LoginType;

/*
 * Created by itzik on 6/8/2014.
 */


public class BFacebookManager {

    private static final String TAG = BFacebookManager.class.getSimpleName();
    private static final boolean DEBUG = Debug.BFacebookManager;

    public static String userFacebookAccessToken;

    private static Completable loginWithFacebook() {

        Map<String, Object> data = new HashMap<String, Object>();

        data.put(DaoDefines.Keys.ThirdPartyData.AccessToken, userFacebookAccessToken);
        data.put(LoginType.TypeKey, AccountType.Facebook);

        return NetworkManager.shared().a.auth.authenticateWithMap(data);
    }

    /** Re authenticate after session state changed.*/
    public static Completable onSessionStateChange(Session session, SessionState state, Exception exception) {

        if (DEBUG) Timber.i("Session changed state");

        if (exception != null)
        {
            exception.printStackTrace();
            if (exception instanceof FacebookOperationCanceledException)
            {
                return Completable.error(ChatError.getError(ChatError.Code.EXCEPTION, exception.getMessage()));
            }
        }

        if (state.isOpened()) {
            if (DEBUG) Timber.i("Session is open.");

            // We will need this session later to make request.
            userFacebookAccessToken = Session.getActiveSession().getAccessToken();

            return loginWithFacebook();
        }
        else  {
            // Logged out of Facebook
            if (DEBUG) Timber.i("Session is closed.");
            return Completable.error(ChatError.getError(ChatError.Code.SESSION_CLOSED, "Facebook session is closed."));
        }
    }

    public static boolean isAuthenticated() {
        return  userFacebookAccessToken != null;
    }

    public static Promise<GraphObject, ChatError, Void> getUserDetails(){

        final Deferred<GraphObject, ChatError, Void> deferred = new DeferredObject<>();
        
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
                            deferred.reject(ChatError.getExceptionError(e));
                        }

                    }
                }
            }).executeAsync();
        } else deferred.reject(new ChatError(ChatError.Code.SESSION_CLOSED));
        
        return deferred.promise();
    }

    /*
    * No need for access token in SDK V3
    * Get the friend list from facebook that is using the app.*/
    public static  Promise<List<GraphUser>, ChatError, Void>  getUserFriendList(){

        final Deferred<List<GraphUser>, ChatError, Void> deferred = new DeferredObject<>();

        
        if (!Session.getActiveSession().getState().isOpened())
        {
            return deferred.reject(new ChatError(ChatError.Code.SESSION_CLOSED));
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
    public static Promise<List<JSONObject>, ChatError, Void> getInvitableFriendsList(){

        final Deferred<List<JSONObject>, ChatError, Void> deferred = new DeferredObject<>();
        
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
                                deferred.reject(new ChatError(ChatError.Code.TAGGED, "Error while fetching invitable friends.", error));
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
            deferred.reject(new ChatError(ChatError.Code.SESSION_CLOSED));
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




