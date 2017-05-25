/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import android.content.Context;
import android.os.Bundle;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.DaoDefines;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.types.AccountType;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
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

        return NM.auth().authenticateWithMap(data);
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

    public static Single<GraphObject> getUserDetails(){
        return Single.create(new SingleOnSubscribe<GraphObject>() {
            @Override
            public void subscribe(final SingleEmitter<GraphObject> ev) throws Exception {
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
                                    ev.onSuccess(user);
                                }
                                catch (Exception e)
                                {
                                    ev.onError(e);
                                }

                            }
                        }
                    }).executeAsync();
                } else {
                    ev.onError(new ChatError(ChatError.Code.SESSION_CLOSED));
                }
            }
        });
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




