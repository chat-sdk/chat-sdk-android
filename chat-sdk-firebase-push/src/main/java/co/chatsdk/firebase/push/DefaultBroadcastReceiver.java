package co.chatsdk.firebase.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.legacy.content.WakefulBroadcastReceiver;

import co.chatsdk.core.error.ChatSDKException;
import co.chatsdk.core.interfaces.BroadcastHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.Configuration;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.utils.AppBackgroundMonitor;
import co.chatsdk.ui.manager.BaseInterfaceAdapter;

/**
 * Created by ben on 5/10/18.
 */

// We want to use this receiver if the app has been killed or if it's in the background
public class DefaultBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ChatSDK.push().getBroadcastHandler() != null) {
            ChatSDK.push().getBroadcastHandler().onReceive(context, intent);
        }
    }
}
