package sdk.chat.message.video;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.licensing.Report;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.chat.options.MediaChatOption;
import sdk.chat.ui.chat.options.MediaType;
import sdk.chat.ui.custom.CustomMessageHandler;

/**
 * Created by ben on 10/6/17.
 */

public class VideoMessageModule extends AbstractModule {

    public static final VideoMessageModule instance = new VideoMessageModule();

    public static VideoMessageModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        Report.shared().add(getName());
        ChatSDK.a().videoMessage = new BaseVideoMessageHandler();
        ChatSDK.ui().addChatOption(new MediaChatOption(context.getResources().getString(R.string.take_video), MediaType.takeVideo()));
        ChatSDK.ui().addChatOption(new MediaChatOption(context.getResources().getString(R.string.choose_video), MediaType.chooseVideo()));

        ChatSDKUI.shared().getMessageCustomizer().addMessageHandler(new CustomMessageHandler() {

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
                        ChatSDKUI.shared().getMessageCustomizer());
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.Video)) {
                    return new VideoMessageHolder(message);
                }
                return null;
            }

            @Override
            public void onClick(ChatActivity activity, View rootView, Message message) {
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
            public void onLongClick(ChatActivity activity, View rootView, Message message) {

            }
        });    }

    @Override
    public String getName() {
        return "VideoMessagesModule";
    }

    @Override
    public MessageHandler getMessageHandler() {
        return ChatSDK.videoMessage();
    }

    @Override
    public List<String> requiredPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        return permissions;
    }

    @Override
    public void stop() {
    }

}
