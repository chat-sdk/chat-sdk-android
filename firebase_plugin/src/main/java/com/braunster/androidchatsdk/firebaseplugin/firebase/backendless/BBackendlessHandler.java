package com.braunster.androidchatsdk.firebaseplugin.firebase.backendless;

import android.content.Context;

import com.backendless.Backendless;
import com.backendless.DeviceRegistration;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.PublishOptions;
import com.backendless.messaging.PushBroadcastMask;
import com.backendless.messaging.PushPolicyEnum;
import com.backendless.services.messaging.MessageStatus;
import com.braunster.androidchatsdk.firebaseplugin.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.interfaces.BPushHandler;
import com.braunster.chatsdk.interfaces.BUploadHandler;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;

import org.jdeferred.Promise;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import timber.log.Timber;

/**
 * Created by Erk on 27.07.2016.
 */
public class BBackendlessHandler implements BPushHandler, BUploadHandler {

    private static final String TAG = BBackendlessHandler.class.getSimpleName();
    private static final boolean DEBUG = Debug.BBackendlessPushHandler;

    private boolean isSubscribed;
    private Context context;

    public void setContext(Context ctx) {
        context = ctx;
    }

    public void initWithAppKey(String appKey, String secretKey, String versionKey)
    {
        Backendless.initApp(context, appKey, secretKey, versionKey);
    }

    @Override
    public boolean subscribeToPushChannel(final String channel) {
        if (!BNetworkManager.sharedManager().getNetworkAdapter().backendlessEnabled())
            return false;

        try {
            Backendless.Messaging.registerDevice(context.getString(R.string.google_project_number), channel, new AsyncCallback<Void>() {
                @Override
                public void handleResponse(Void response) {
                    if(DEBUG) Timber.v("Device has been subscribed to channel " + channel);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    if(DEBUG) Timber.v("Device subscription failed. " + fault.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean unsubscribeToPushChannel(String channel) {
        if (!BNetworkManager.sharedManager().getNetworkAdapter().backendlessEnabled())
            return false;

        // TODO: unsubscribe from push channel backendless
        // http://support.backendless.com/topic/push-notification-unregister-from-a-specific-channel
        DeviceRegistration devReg = null;

        try {
            devReg = Backendless.Messaging.getDeviceRegistration();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(devReg != null) {
            Backendless.Messaging.unregisterDevice();
        }

        return true;
    }

    @Override
    public void pushToChannels(Collection<String> channels, JSONObject data) {
        // Configure the header
        PublishOptions publishOptions = new PublishOptions();
        try {
        publishOptions.putHeader("android-ticker-text", data.getString(BDefines.Keys.CONTENT));
        publishOptions.putHeader("android-content-title", "Message from " + data.getString(BDefines.Keys.MESSAGE_SENDER_NAME));
        publishOptions.putHeader("android-content-text", data.getString(BDefines.Keys.MESSAGE_PAYLOAD));
        publishOptions.setPublisherId(data.getString(BDefines.Keys.MESSAGE_SENDER_ENTITY_ID));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Only push to android devices
        DeliveryOptions deliveryOptions = new DeliveryOptions();
        deliveryOptions.setPushPolicy(PushPolicyEnum.ONLY);
        deliveryOptions.setPushBroadcast(PushBroadcastMask.ALL);

        // Publish a push notification to each channel
        for(final String channel : channels) {
            try {
                data.put(BDefines.Keys.Channel, channel);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Backendless.Messaging.publish(channel, data.toString(), publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus response) {
                    if (DEBUG) Timber.v("Message published to channel: " + channel);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    if (DEBUG) Timber.v("Publish failed, " + fault.getMessage());
                }
            });
        }
    }

    @Override
    public Promise uploadFile(byte[] data, String name, String mimeType) {
        return null;
    }
}
