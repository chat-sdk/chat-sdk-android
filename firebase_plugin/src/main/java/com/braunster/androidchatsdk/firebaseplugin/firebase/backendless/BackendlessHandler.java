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

import co.chatsdk.core.dao.DaoDefines;
import co.chatsdk.core.defines.Debug;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import co.chatsdk.core.handlers.PushHandler;
import timber.log.Timber;

/**
 * Created by Erk on 27.07.2016.
 */
public class BackendlessHandler implements PushHandler {

    private static final String TAG = BackendlessHandler.class.getSimpleName();
    private static final boolean DEBUG = Debug.BackendlessPushHandler;

    //private boolean isSubscribed;
    private Context context;

    public BackendlessHandler(Context ctx, String appKey, String secretKey, String versionKey)
    {
        context = ctx;
        Backendless.initApp(ctx, appKey, secretKey, versionKey);
    }

    @Override
    public boolean subscribeToPushChannel(final String channel) {
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
        publishOptions.putHeader("android-ticker-text", data.getString(DaoDefines.Keys.CONTENT));
        publishOptions.putHeader("android-content-title", "CoreMessage from " + data.getString(DaoDefines.Keys.MESSAGE_SENDER_NAME));
        publishOptions.putHeader("android-content-text", data.getString(DaoDefines.Keys.MESSAGE_PAYLOAD));
        publishOptions.setPublisherId(data.getString(DaoDefines.Keys.MESSAGE_SENDER_ENTITY_ID));
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
                data.put(DaoDefines.Keys.Channel, channel);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Backendless.Messaging.publish(channel, data.toString(), publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus response) {
                    if (DEBUG) Timber.v("CoreMessage published to channel: " + channel);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    if (DEBUG) Timber.v("Publish failed, " + fault.getMessage());
                }
            });
        }
    }

}
