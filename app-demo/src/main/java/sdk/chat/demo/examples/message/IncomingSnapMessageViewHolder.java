package sdk.chat.demo.examples.message;

import android.view.View;
import android.widget.TextView;

import sdk.chat.demo.pre.R;
import sdk.chat.ui.view_holders.base.BaseIncomingTextMessageViewHolder;

public class IncomingSnapMessageViewHolder extends BaseIncomingTextMessageViewHolder<SnapMessageHolder> {

    TextView username;

    public IncomingSnapMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        username = itemView.findViewById(R.id.userName);
    }

    @Override
    public void onBind(SnapMessageHolder message) {
        super.onBind(message);

        String value = message.customValue;
        username.setText(value);

    }
}
