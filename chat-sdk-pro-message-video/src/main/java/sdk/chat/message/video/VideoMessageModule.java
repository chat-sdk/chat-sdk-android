package sdk.chat.message.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.message.video.R;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.chat.options.MediaChatOption;
import co.chatsdk.ui.chat.options.MediaType;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.custom.IMessageHandler;

/**
 * Created by ben on 10/6/17.
 */

public class VideoMessageModule implements Module {

    public static final VideoMessageModule instance = new VideoMessageModule();

    public static VideoMessageModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().videoMessage = new BaseVideoMessageHandler();
        ChatSDK.ui().addChatOption(new MediaChatOption(context.getResources().getString(R.string.take_video), MediaType.takeVideo()));
        ChatSDK.ui().addChatOption(new MediaChatOption(context.getResources().getString(R.string.choose_video), MediaType.chooseVideo()));

        Customiser.shared().addMessageHandler(new IMessageHandler() {
            @Override
            public boolean hasContentFor(MessageHolder message, byte type) {
                return type == MessageType.Video && message instanceof VideoMessageHolder;
            }

            @Override
            public void onBindMessageHolders(Context ctx, MessageHolders holders) {
                holders.registerContentType(
                        (byte) MessageType.Video,
                        IncomingVideoMessageViewHolder.class,
                        R.layout.view_holder_incoming_video_message,
                        OutcomingVideoMessageViewHolder.class,
                        R.layout.view_holder_outcoming_video_message, this);
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.Video)) {
                    return new VideoMessageHolder(message);
                }
                return null;
            }

            @Override
            public void onClick(Activity activity, View rootView, Message message) {
                if (message.getMessageType().is(MessageType.Video)) {
                    String videoURL = (String) message.valueForKey(Keys.MessageVideoURL);
                    if(videoURL != null) {
                        Intent intent = new Intent(activity, PlayVideoActivity.class);
                        intent.putExtra(Keys.IntentKeyFilePath, videoURL);
                        activity.startActivity(intent);
                    }
                }
            }

            @Override
            public void onLongClick(Activity activity, View rootView, Message message) {

            }
        });    }

    @Override
    public String getName() {
        return "VideoMessagesModule";
    }
}
