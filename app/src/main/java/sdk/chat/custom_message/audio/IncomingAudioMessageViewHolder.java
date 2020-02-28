package sdk.chat.custom_message.audio;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.stfalcon.chatkit.messages.MessageHolders;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.android.app.R;
import co.chatsdk.core.audio.AudioPlayer;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.NameBinder;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.icons.Icons;

public class IncomingAudioMessageViewHolder extends MessageHolders.IncomingTextMessageViewHolder<AudioMessageHolder> {

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.userName) protected TextView userName;
    @BindView(R.id.audioPlayerView) protected AudioPlayerView audioPlayerView;

    public IncomingAudioMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(AudioMessageHolder message) {
        super.onBind(message);

        OnlineStatusBinder.bind(onlineIndicator, message);
        NameBinder.bind(userName, message);

        try {
            audioPlayerView.bind(message.audioURL());
//            audioPlayerView.setTotalTime(message.length());
//            audioPlayerView.setCurrentTime("0:00");
        } catch (Exception e) {
            ChatSDK.logError(e);
        }

    }

    protected Object getPayloadForImageLoader(AudioMessageHolder message) {
        return message;
    }
}
