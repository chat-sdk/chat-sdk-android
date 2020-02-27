package sdk.chat.custom_message.audio;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.android.app.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.MessageBinder;
import co.chatsdk.ui.binders.ReadStatusViewBinder;
import co.chatsdk.ui.icons.Icons;

public class OutcomingAudioMessageViewHolder extends MessageHolders.OutcomingTextMessageViewHolder<AudioMessageHolder> {

    @BindView(R2.id.readStatus) protected ImageView readStatus;

    public OutcomingAudioMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(AudioMessageHolder holder) {
        super.onBind(holder);

        ReadStatusViewBinder.onBind(readStatus, holder);
        MessageBinder.onBindSendStatus(time, holder);


    }

    protected Object getPayloadForImageLoader(AudioMessageHolder message) {
        return message;
    }

}
