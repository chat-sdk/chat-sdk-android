package sdk.chat.ui.binders;

import android.view.View;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.MessageHolder;

public class OnlineStatusBinder {

    public void bind(View onlineIndicator, MessageHolder holder) {
        bind(onlineIndicator, holder.getUser().isOnline());
    }

    public void bind(View onlineIndicator, boolean isOnline) {
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
