package sdk.chat.message.audio;

import android.view.View;

import sdk.chat.ui.view_holders.v2.BaseMessageViewHolder;
import sdk.chat.ui.view_holders.v2.MessageDirection;

public class OutcomingAudioMessageViewHolder extends BaseMessageViewHolder<AudioMessageHolder> {

    protected AudioPlayerView audioPlayerView;

    public OutcomingAudioMessageViewHolder(View itemView) {
        super(itemView, MessageDirection.Outcoming);
        audioPlayerView = itemView.findViewById(R.id.audioPlayerView);
    }

    @Override
    public void onBind(AudioMessageHolder message) {
        super.onBind(message);

        audioPlayerView.buttonColor = R.color.white;
        audioPlayerView.sliderTrackColor = R.color.blue_light;
        audioPlayerView.sliderThumbColor = R.color.white;
        audioPlayerView.textColor = R.color.white;
        audioPlayerView.bind(message.audioURL(), message.getTotalTime());

    }
}
