package com.braunster.chatsdk.network;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.braunster.chatsdk.object.BError;
import com.firebase.client.AuthData;

import org.apache.commons.lang3.StringUtils;
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

import static com.braunster.chatsdk.network.BDefines.BAccountType.Twitter;
import static com.braunster.chatsdk.network.BDefines.Prefs.LoginTypeKey;


/**
 * Created by braunster on 04/08/14.
 */
public class TwitterManager {

    private static final String TAG = TwitterManager.class.getSimpleName();

    private static final boolean DEBUG = Debug.TwitterManager;

    public static final int ERROR = 10, SUCCESS = 20;

    public static Long userId = -1L;

    public static String profileImageUrl = "";

    // Twitter Urls.
    private static final String PROTECTED_RESOURCE_URL = "https://api.twitter.com/1.1/account/verify_credentials.json";
    private static final String USERS_SHOW_URL = "https://api.twitter.com/1.1/users/show.json?user_id=";

    private static OAuthService service;
    private static Token requestToken;
    private static Token accessToken;

    public static Thread getAuthorizationURLThread(final Handler handler){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                // If you choose to use a callback, "oauth_verifier" will be the return value by Twitter (request param)
                if (service == null)
                    service = createService();

                try {
                    requestToken = service.getRequestToken();
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    Message message = new Message();
                    message.what = ERROR;
                    message.obj = BError.getError(BError.Code.BAD_RESPONSE, "Cant get request token.");
                    handler.sendMessage(message);
                    return;
                }

                if (DEBUG) Log.d(TAG, "Token, " + requestToken.getToken());

                String authrizationURL =  service.getAuthorizationUrl(requestToken);

                Message message = new Message();
                message.obj = authrizationURL;
                message.what = SUCCESS;
                handler.sendMessage(message);
            }
        });
    }

    public static Thread getVerifierThread(final String ver, final CompletionListenerWithDataAndError<AuthData, Object> listener){
        return new Thread(new Runnable() {
            @Override
            public void run() {
                if (DEBUG) Log.d(TAG, "Verifier: " + ver);
                accessToken = verify(ver);

                if (accessToken == null)
                {
                    if (listener != null)
                        handler.sendMessage(MessageObj.getErrorMessage(listener, BError.getError(BError.Code.ACCESS_TOKEN_REFUSED, "Access token is null")));
                    return;
                }

                Response response = getReponse(PROTECTED_RESOURCE_URL);

                if (!response.isSuccessful())
                {
                    if (listener != null)
                        handler.sendMessage(MessageObj.getErrorMessage(listener, BError.getError(BError.Code.BAD_RESPONSE, response.getBody())));
                    return;
                }

                if (DEBUG) Log.d(TAG, "Header: " + response.getHeader("id"));
                if (DEBUG) Log.d(TAG, "response" + ", Message" + response.getMessage() + ", Body" + response.getBody());

                try {
                    JSONObject json = new JSONObject(response.getBody());
                    userId = json.getLong("id");
                    profileImageUrl = json.getString(BDefines.Keys.ThirdPartyData.ImageURL);
                    BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(
                            FirebasePaths.getMap(new String[]{BDefines.Keys.UserId, LoginTypeKey}, json.get("id"), Twitter), listener);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        );
    }

    public static void getUserDetails(final CompletionListenerWithData<Response> listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = getReponse(USERS_SHOW_URL + userId);

                if (response == null){
                    handler.sendMessage(MessageObj.getErrorMessage(listener, BError.getError(BError.Code.BAD_RESPONSE, "response is null")));
                    return;
                }
                if (DEBUG) Log.d(TAG, "Header: " + response.getHeader("id"));

                if (DEBUG) Log.d(TAG, "response, succsesful: " + response.isSuccessful()  + ", Message" + response.getMessage() + ", Body" + response.getBody());

                if (response.isSuccessful())
                {
                    try {
                        JSONObject json = new JSONObject(response.getBody());
                        userId = json.getLong("id");
                        profileImageUrl = json.getString(BDefines.Keys.ThirdPartyData.ImageURL);
                        handler.sendMessage(MessageObj.getSuccessMessage(listener, response));
                    } catch (JSONException e) {
                        handler.sendMessage(MessageObj.getErrorMessage(listener, BError.getError(BError.Code.BAD_RESPONSE, response.getBody())));
                        return;
                    }
                }
                else{
                    handler.sendMessage(MessageObj.getErrorMessage(listener, BError.getError(BError.Code.BAD_RESPONSE, response.getBody())));
                    return;
                }
            }
        }).start();

    }

    /** Must be called from inside a thread.*/
    private static Response getReponse(String url){
        if (service == null)
            service = createService();

        OAuthRequest request =  new OAuthRequest(Verb.GET, url);

        if (accessToken == null)
        {
            accessToken = new Token(BDefines.APIs.TwitterAccessToken, BDefines.APIs.TwitterAccessTokenSecret);
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

    private static OAuthService createService(){
        // If you choose to use a callback, "oauth_verifier" will be the return value by Twitter (request param)
        return service = new ServiceBuilder()
                .provider(TwitterApi.SSL.class)
                .apiKey(BDefines.APIs.TwitterConsumerKey)
                .apiSecret(BDefines.APIs.TwitterConsumerSecret)
                .callback("http://androidchatsdktwitter.com")
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

    static Handler handler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case 1:
                    MessageObj obj = (MessageObj) msg.obj;
                    if (obj.listener instanceof CompletionListenerWithData)
                    {
                        CompletionListenerWithData listenerWithData = ((CompletionListenerWithData) obj.listener);
                        if (msg.arg1 == ERROR)
                            listenerWithData.onDoneWithError(((BError) obj.data));
                        else listenerWithData.onDone(obj.data);
                    }
            }
        }
    };
}
