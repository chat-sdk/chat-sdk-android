package sdk.chat.message.audio;

import android.view.View;

import sdk.chat.ui.view_holders.v2.BaseMessageViewHolder;
import sdk.chat.ui.view_holders.v2.MessageDirection;

public class IncomingAudioMessageViewHolder extends BaseMessageViewHolder<AudioMessageHolder> {

    protected AudioPlayerView audioPlayerView;

    public IncomingAudioMessageViewHolder(View itemView) {
        super(itemView, MessageDirection.Incoming);
        audioPlayerView = itemView.findViewById(R.id.audioPlayerView);
    }

    @Override
    public void onBind(AudioMessageHolder message) {
        super.onBind(message);
        audioPlayerView.bind(message.audioURL(), message.getTotalTime());
    }
}
