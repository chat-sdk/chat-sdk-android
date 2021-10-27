package sdk.chat.app.xmpp.wow;

import android.view.View;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.xmpp.R;
import sdk.chat.ui.binders.OnlineStatusBinder;

public class WowOnlineStatusBinder extends OnlineStatusBinder {

    public void bind(View onlineIndicator, boolean isOnline) {
        if (onlineIndicator.getVisibility() == View.GONE) {
            return;
        }
        if (ChatSDK.config().disablePresence) {
            onlineIndicator.setVisibility(View.GONE);
        } else {
            onlineIndicator.setVisibility(View.VISIBLE);
            if (isOnline) {
                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
            } else {
                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
            }
        }
    }

}
