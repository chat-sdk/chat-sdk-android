package sdk.chat.audio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.message.audio.R;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.custom.IMessageHandler;

/**
 * Created by ben on 9/28/17.
 */

public class AudioMessageModule implements Module {

    public static final AudioMessageModule instance = new AudioMessageModule();

    public static AudioMessageModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().audioMessage = new BaseAudioMessageHandler();

        AndroidAudioConverter.load(context, new ILoadCallback() {
            @Override
            public void onSuccess() {
                ChatSDK.a().audioMessage.setCompressionEnabled(true);
            }

            @Override
            public void onFailure(Exception error) {
                ChatSDK.events().onError(error);
            }
        });

        Customiser.shared().addMessageHandler(new IMessageHandler() {
            @Override
            public boolean hasContentFor(MessageHolder message, byte type) {
                return type == MessageType.Audio && message instanceof AudioMessageHolder;
            }

            @Override
            public void onBindMessageHolders(Context ctx, MessageHolders holders) {
                holders.registerContentType(
                        (byte) MessageType.Audio,
                        IncomingAudioMessageViewHolder.class,
                        R.layout.view_holder_incoming_audio_message,
                        OutcomingAudioMessageViewHolder.class,
                        R.layout.view_holder_outcoming_audio_message, this);
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.Audio)) {
                    return new AudioMessageHolder(message);
                }
                return null;
            }

            @Override
            public void onClick(Activity activity, View rootView, Message message) {
            }

            @Override
            public void onLongClick(Activity activity, View rootView, Message message) {

            }
        });
    }

    @Override
    public String getName() {
        return "AudioMessageModule";
    }
}
