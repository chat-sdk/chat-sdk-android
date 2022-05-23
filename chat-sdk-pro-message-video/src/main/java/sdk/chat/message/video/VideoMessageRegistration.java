package sdk.chat.message.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.DownloadablePayload;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.DefaultMessageRegistration;

public class VideoMessageRegistration extends DefaultMessageRegistration {

    @Override
    public List<Byte> getTypes() {
        return types(MessageType.Video);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(VideoMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context ctx, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Video,
                IncomingVideoMessageViewHolder.class,
                R.layout.view_holder_incoming_video_message,
                OutcomingVideoMessageViewHolder.class,
                R.layout.view_holder_outcoming_video_message,
                ChatSDKUI.shared().getMessageRegistrationManager());
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.getMessageType().is(MessageType.Video)) {
            return new VideoMessageHolder(message);
        }
        return null;
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        if (!super.onClick(activity, rootView, message)) {
            if (message.getMessageType().is(MessageType.Video)) {

                MessagePayload payload = ChatSDK.getMessagePayload(message);
                if (payload instanceof DownloadablePayload) {
                    DownloadablePayload dp = (DownloadablePayload) payload;
                    if (dp.canDownload()) {
                        dp.startDownload().subscribe();
                        return true;
                    }
                }

//                String videoURL = (String) message.valueForKey(Keys.MessageVideoURL);
                String videoURL = message.getFilePath();
                if(videoURL != null) {
                    Intent intent = new Intent(activity, VideoMessageModule.config().getVideoPlayerActivity());
                    intent.putExtra(Keys.IntentKeyFilePath, videoURL);
                    activity.startActivity(intent);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onLongClick(Activity activity, View rootView, Message message) {
        return false;
    }
}
