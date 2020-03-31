package co.chatsdk.ui.view_holders;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.model.SystemMessageHolder;

public class SystemMessageViewHolder extends MessageHolders.BaseMessageViewHolder<SystemMessageHolder> {

    @BindView(R2.id.bubble) protected ViewGroup bubble;
    @BindView(R2.id.messageText) protected TextView messageText;

    protected View itemView;

    public SystemMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        this.itemView = itemView;
        bindButterKnife();
    }

    protected void bindButterKnife() {
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(SystemMessageHolder systemMessageHolder) {
        messageText.setText(systemMessageHolder.getText());
    }

}
