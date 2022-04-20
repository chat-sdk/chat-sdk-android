package sdk.chat.message.audio;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.DefaultMessageRegistration;

public class AudioMessageRegistration extends DefaultMessageRegistration {
    @Override
    public List<Byte> getTypes() {
        return types(MessageType.Audio);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(AudioMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context ctx, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Audio,
                IncomingAudioMessageViewHolder.class,
                R.layout.view_holder_incoming_audio_message,
                OutcomingAudioMessageViewHolder.class,
                R.layout.view_holder_outcoming_audio_message, ChatSDKUI.shared().getMessageRegistrationManager());
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.getMessageType().is(MessageType.Audio)) {
            return new AudioMessageHolder(message);
        }
        return null;
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        return super.onClick(activity, rootView, message);
    }

    @Override
    public boolean onLongClick(Activity activity, View rootView, Message message) {
        return false;
    }
}
