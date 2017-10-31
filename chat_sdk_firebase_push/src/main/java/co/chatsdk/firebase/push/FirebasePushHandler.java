package co.chatsdk.firebase.push;

import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.handlers.PushHandler;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.NM;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by ben on 9/1/17.
 */

public class FirebasePushHandler implements PushHandler {

    private boolean authFinished = false;
    private TokenPusher pusher;
    private String token;

    public FirebasePushHandler (TokenPusher pusher) {
        this.pusher = pusher;

        Hook authHook = new Hook(new Hook.Executor() {
            @Override
            public void execute(HashMap<String, Object> data) {
                authFinished = true;
                if(updatePushToken()) {
                    pushToken();
                }
            }
        });

        NM.hook().addHook(authHook, BaseHookHandler.UserAuthFinished);

        TokenChangeConnector.shared().addListener(new InstanceIdService.TokenChangeListener() {
            @Override
            public void updated(String token) {

                FirebasePushHandler.this.token = token;

                if(authFinished && updatePushToken()) {
                    pushToken();
                }
            }
        });

    }

    public void pushToken () {
        if(pusher != null) {
            pusher.pushToken();
        }
    }

    public boolean updatePushToken () {
        if(token != null && token.length() > 0 && NM.currentUser() != null) {
            String currentToken = NM.currentUser().metaStringForKey(Keys.PushToken);
            if(currentToken == null || !currentToken.equals(token)) {
                NM.currentUser().setMetaString(Keys.PushToken, token);
                return true;
            }
        }
        return false;
    }


    @Override
    public void subscribeToPushChannel(String channel) {
        FirebaseMessaging.getInstance().subscribeToTopic(safeChannel(channel));
    }

    @Override
    public void unsubscribeToPushChannel(String channel) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(safeChannel(channel));
    }

    @Override
    public void pushToChannels(List<String> channels, Map<String, String> data) {

        for(String channel : channels) {
            pushToChannel(channel, data).doOnError(new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    throwable.printStackTrace();
                }
            }).subscribe();
        }
    }

    private Completable pushToChannel (final String channel, final Map<String, String> data) {
        return Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter e) throws Exception {
                final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                String serverKey = "key=" + ChatSDK.config().firebaseCloudMessagingServerKey;

                HashMap<String, Object> params = new HashMap<>();

                params.put("to", channel);
                params.put("notification", data);

                String json = new JSONObject(params).toString();

                OkHttpClient client = new OkHttpClient();

                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url("https://fcm.googleapis.com/fcm/send")
                        .header("Authorization", serverKey)
                        .post(body).build();

                Response response = client.newCall(request).execute();

                e.onComplete();
            }
        }).subscribeOn(Schedulers.single());
    }

    @Override
    public void pushToUsers(List<User> users, Message message) {
        ArrayList<String> channels = new ArrayList<>();

        User currentUser = NM.currentUser();

        for(User user : users) {
            String pushToken = user.metaStringForKey(Keys.PushToken);
            if(!user.equals(currentUser) && !StringChecker.isNullOrEmpty(pushToken)) {
                channels.add(pushToken);
            }
        }

        String text = Strings.payloadAsString(message);

        HashMap<String, String> data = new HashMap<>();
        data.put("body", text);
        data.put("title", message.getSender().getName());

        pushToChannels(channels, data);

    }

    public interface TokenPusher {
        void pushToken ();
    }

    public String safeChannel (String channel) {
        return channel.replace("@", "a").replace(".", "d");
    }
}
