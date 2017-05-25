/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.network;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.braunster.chatsdk.R;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.core.DaoDefines;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.LoginType;
import co.chatsdk.core.defines.Debug;
import com.braunster.chatsdk.object.ChatError;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.Deferred;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;


/**
 * Created by braunster on 04/08/14.
 */
public class TwitterManager {

    private static final String TAG = TwitterManager.class.getSimpleName();

    private static final boolean DEBUG = Debug.TwitterManager;

    public static final int ERROR = 10, SUCCESS = 20;

    public static String userId = "-1L";

    public static String profileImageUrl = "";

    // Twitter Urls.
    private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";

    private static OAuthService service;
    private static Token requestToken;
    public static Token accessToken;

    public static Thread getAuthorizationURLThread(final Context context, final Handler handler){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                // If you choose to use a callback, "oauth_verifier" will be the return value by Twitter (request param)
                if (service == null)
                    service = createService(context);

                try {
                    requestToken = service.getRequestToken();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    Message message = new Message();
                    message.what = ERROR;
                    message.obj = ChatError.getError(ChatError.Code.BAD_RESPONSE, "Cant get request token.");
                    handler.sendMessage(message);
                    return;
                }

                String authrizationURL =  service.getAuthorizationUrl(requestToken);

                Message message = new Message();
                message.obj = authrizationURL;
                message.what = SUCCESS;
                handler.sendMessage(message);
            }
        });
    }

    // TODO: Refactor this
    public static Thread getVerifierThread(final Context context, final String ver, final Deferred<Object, ChatError, Void> deferred){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                accessToken = verify(ver);

                if (accessToken == null)
                {
                    handler.sendMessage(MessageObj.getErrorMessage(deferred, ChatError.getError(ChatError.Code.ACCESS_TOKEN_REFUSED, "Access token is null")));
                    return;
                }

                Response response = getResponse(context, PROTECTED_RESOURCE_URL);

                if (!response.isSuccessful())
                {
                    handler.sendMessage(MessageObj.getErrorMessage(deferred, ChatError.getError(ChatError.Code.BAD_RESPONSE, response.getBody())));
                    return;
                }

                try {
                    JSONObject json = new JSONObject(response.getBody());

                    if (DEBUG) Timber.d("Twitter Response: %s", json.toString());

                    userId = json.getString("id");

                    profileImageUrl = json.getString(DaoDefines.Keys.ThirdPartyData.ImageURL);

                    if (DEBUG) Timber.i("profileImageUrl: %s", profileImageUrl);

                    final Map<String, Object> data = new HashMap<String, Object>();

                    data.put(DaoDefines.Keys.UserId, json.get("id"));
                    data.put(LoginType.TypeKey, AccountType.Twitter);

                    NetworkManager.shared().a.auth.authenticateWithMap(data).subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onComplete() {
                            deferred.resolve(null);
                        }

                        @Override
                        public void onError(Throwable e) {
                            deferred.reject(ChatError.getError(0, e.getMessage()));
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        );
    }

    /** Must be called from inside a thread.*/
    private static Response getResponse(Context context, String url){
        if (service == null)
            service = createService(context);

        OAuthRequest request =  new OAuthRequest(Verb.GET, url);

        if (accessToken == null)
        {
            accessToken = new Token(context.getString(R.string.twitter_access_token), context.getString(R.string.twitter_access_token_secret));
        }

        service.signRequest(accessToken, request);

        return request.send();
    }

    private static Token verify(String ver){
        if (StringUtils.isEmpty(ver))
            return null;

        Verifier verifier = new Verifier(ver);

        try {
            return service.getAccessToken(requestToken, verifier);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static OAuthService createService(Context context){
        // If you choose to use a callback, "oauth_verifier" will be the return value by Twitter (request param)
        return service = new ServiceBuilder()
                .provider(TwitterApi.class)
                .apiKey(context.getString(R.string.twitter_consumer_key))
                .apiSecret(context.getString(R.string.twitter_consumer_secret))
                .callback(context.getString(R.string.twitter_callback_url))
                .debug()
                .build();
    }

    private static class MessageObj{
        public Object listener;
        public Object data;

        private MessageObj(Object listener, Object data) {
            this.listener = listener;
            this.data = data;
        }

        private static Message getErrorMessage(Object listener, Object data){
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = ERROR;
            msg.obj = new MessageObj(listener, data);
            return msg;
        }

        private static Message getSuccessMessage(Object listener, Object data){
            Message msg = new Message();
            msg.what = 1;
            msg.arg1 = SUCCESS;
            msg.obj = new MessageObj(listener, data);
            return msg;
        }
    }

    private static TwitterHandler handler = new TwitterHandler();

    private static class TwitterHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 1:
                    MessageObj obj = (MessageObj) msg.obj;
                    if (obj.listener instanceof Deferred)
                    {
                        Deferred<Object, ChatError, Void> deferred= ((DeferredObject) obj.listener);


                        if (msg.arg1 == ERROR)
                            deferred.reject((ChatError) obj.data);
                        else deferred.resolve(obj.data);
                    }
            }
        }
    }
    
    public static Map<String, Object> getMap(String[] keys,  Object...values){
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0 ; i < keys.length; i++){

            // More values then keys entered.
            if (i == values.length)
                break;

            map.put(keys[i], values[i]);
        }

        return map;
    }
}

