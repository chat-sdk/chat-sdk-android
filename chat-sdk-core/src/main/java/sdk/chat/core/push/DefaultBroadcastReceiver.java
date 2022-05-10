package sdk.chat.core.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import sdk.chat.core.session.ChatSDK;

/**
 * Created by ben on 5/10/18.
 */

// We want to use this receiver if the app has been killed or if it's in the background
public class DefaultBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        android.os.Debug.waitForDebugger();

        if (ChatSDK.shared().isValid() && !ChatSDK.config().manualPushHandlingEnabled ) {
            for (BroadcastHandler handler: ChatSDK.shared().broadcastHandlers()) {
                if (handler.onReceive(context, intent)) {
                    break;
                }
            }
        }
    }
}
