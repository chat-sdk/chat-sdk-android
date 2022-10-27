package sdk.chat.firebase.push;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import sdk.chat.core.push.BroadcastHandler;
import sdk.chat.core.session.ChatSDK;

public class DefaultMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (ChatSDK.shared().isValid() && !ChatSDK.config().manualPushHandlingEnabled ) {
            for (BroadcastHandler handler: ChatSDK.shared().broadcastHandlers()) {
                if (handler.onReceive(getApplicationContext(), remoteMessage.toIntent())) {
                    break;
                }
            }
        }
    }

}
