package sdk.chat.ui.view_holders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.SystemMessageHolder;

public class SystemMessageViewHolder extends MessageHolders.BaseMessageViewHolder<SystemMessageHolder> {

    protected ViewGroup bubble;
    protected TextView messageText;

    protected View itemView;

    public SystemMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.itemView = itemView;

        bubble = itemView.findViewById(R.id.bubble);
        messageText = itemView.findViewById(R.id.messageText);
    }

    @Override
    public void onBind(SystemMessageHolder systemMessageHolder) {
        bubble.setSelected(isSelected());
        messageText.setText(systemMessageHolder.getText());
    }

}
