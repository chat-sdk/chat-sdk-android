package com.braunster.chatsdk.Utils;

import android.os.Handler;
import android.util.Log;


/**
 * Created by braunster on 03/08/14.
 */
public class TwitterUtils {
    private static final String TAG= TwitterUtils.class.getSimpleName();
    private static final boolean DEBUG = true;

    private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";

    private Handler handler;

    public static void login(){
        if (DEBUG) Log.v(TAG, "login");
/*
        new Thread(new Runnable() {
            @Override
            public void run() {
                // If you choose to use a callback, "oauth_verifier" will be the return value by Twitter (request param)
                OAuthService service = new ServiceBuilder()
                        .provider(TwitterApi.class)
                        .apiKey(BDefines.APIs.TwitterConsumerKey)
                        .apiSecret(BDefines.APIs.TwitterConsumerSecret)
                        .build();
                Scanner in = new Scanner(System.in);

                Token requestToken = service.getRequestToken();

                if (DEBUG) Log.d(TAG, "Token, " + requestToken.getToken());

                String authrizationURL =  service.getAuthorizationUrl(requestToken);
                if (DEBUG) Log.d(TAG, "authrizationURL, " + authrizationURL);

                if (in.hasNext())
                {
                    Verifier verifier = new Verifier(in.nextLine());
                    Token accessToken = service.getAccessToken(requestToken, verifier);
                    OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);

                    service.signRequest(accessToken, request);
                    Response response = request.send();
                }
                else if (DEBUG) Log.e(TAG, "no next");
            }
        }).start();*/
    }
}
