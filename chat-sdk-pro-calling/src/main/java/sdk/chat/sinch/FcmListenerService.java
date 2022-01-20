package sdk.chat.sinch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sinch.android.rtc.NotificationResult;
import com.sinch.android.rtc.SinchHelpers;
import com.sinch.android.rtc.calling.CallNotificationResult;

import java.util.Map;

public class FcmListenerService extends FirebaseMessagingService {

    public static String CHANNEL_ID = "Sinch Push Notification Channel";
    private static final String TAG = FcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Map data = remoteMessage.getData();

        // Optional: inspect the payload w/o starting Sinch Client and thus avoiding onIncomingCall()
        // e.g. useful to fetch user related polices (blacklist), resources (to show a picture, etc).
        NotificationResult result = SinchHelpers.queryPushNotificationPayload(getApplicationContext(), data);
        if (result.isValid() && result.isCall()) {
            CallNotificationResult callResult = result.getCallResult();
            Log.d(TAG, "queryPushNotificationPayload() -> display name: " + result.getDisplayName());
            if (callResult != null) {
                Log.d(TAG, "queryPushNotificationPayload() -> headers: " + result.getCallResult().getHeaders());
                Log.d(TAG, "queryPushNotificationPayload() -> remote user ID: " + result.getCallResult().getRemoteUserId());
            }
        }

        // Mandatory: forward payload to the SinchClient.
        if (SinchHelpers.isSinchPushPayload(data)) {
            new ServiceConnection() {
                private Map payload;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (payload != null) {
                        SinchService sinchService = SinchModule.shared().sinchService;
                        if (sinchService != null) {
                            NotificationResult result = sinchService.client().relayRemotePushNotificationPayload(payload);
                            if (result.isValid() && result.isCall()) {
                                // Optional: handle result, e.g. show a notification or similar.
                            }
                        }
                    }
                    payload = null;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {}

                public void relayMessageData(Map<String, String> data) {
                    payload = data;
                    createNotificationChannel(NotificationManager.IMPORTANCE_MAX);
                    getApplicationContext().bindService(new Intent(getApplicationContext(), SinchService.class), this, BIND_AUTO_CREATE);
                }
            }.relayMessageData(data);
        }
    }

    private void createNotificationChannel(int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Sinch";
            String description = "Incoming Sinch Push Notifications.";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}