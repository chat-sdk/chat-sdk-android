package co.chatsdk.ui.binders;

import android.view.View;

import co.chatsdk.ui.R;

public class OnlineStatusBinder {
    public static void bind(View onlineIndicator, boolean isOnline) {
        if (isOnline) {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_online);
        } else {
            onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_offline);
        }

    }
}
