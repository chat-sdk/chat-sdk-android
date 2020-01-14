package co.chatsdk.ui.chatkit.custom;

import android.view.View;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import co.chatsdk.ui.R;
import co.chatsdk.ui.chatkit.model.ThreadHolder;

public class DialogViewHolder extends DialogsListAdapter.DialogViewHolder<ThreadHolder> {

    private View onlineIndicator;

    public DialogViewHolder(View itemView) {
        super(itemView);
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
    }

    @Override
    public void onBind(ThreadHolder dialog) {
        super.onBind(dialog);

        if (dialog.getUsers().size() > 1) {
            onlineIndicator.setVisibility(View.GONE);
        } else {
            boolean isOnline = dialog.getOtherUser().isOnline();
            onlineIndicator.setVisibility(View.VISIBLE);
            if (isOnline) {
                onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_online);
            } else {
                onlineIndicator.setBackgroundResource(R.drawable.chatkit_shape_bubble_offline);
            }
        }
    }
}
