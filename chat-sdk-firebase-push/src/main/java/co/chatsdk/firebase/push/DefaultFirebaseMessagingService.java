package co.chatsdk.firebase.push;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.pmw.tinylog.Logger;


public class DefaultFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Logger.info(remoteMessage);
    }

}
