package sdk.chat.audio;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
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
        return ChatSDK.shared().getString(co.chatsdk.ui.R.string.audio_message);
    }

}
