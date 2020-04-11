package sdk.chat.audio;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.chat.model.MessageHolder;

public class AudioMessageHolder extends MessageHolder implements MessageContentType {

    public AudioMessageHolder(Message message) {
        super(message);
    }

    public String audioURL() {
        return message.stringForKey(Keys.MessageAudioURL);
    }

    @Override
    public String getText() {
        return ChatSDK.getString(co.chatsdk.ui.R.string.audio_message);
    }

}
