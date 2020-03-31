package co.chatsdk.ui.binders;

import android.view.View;

import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.model.MessageHolder;

public class OnlineStatusBinder {

    public static void bind(View onlineIndicator, MessageHolder holder) {
        bind(onlineIndicator, holder.getUser().isOnline());
    }

    public static void bind(View onlineIndicator, boolean isOnline) {
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline);
        }

    }
}
