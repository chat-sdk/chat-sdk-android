package sdk.chat.audio;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.chat.model.MessageHolder;
import sdk.chat.core.utils.CurrentLocale;

public class AudioMessageHolder extends MessageHolder implements MessageContentType {

    public AudioMessageHolder(Message message) {
        super(message);
    }

    public String audioURL() {
        return message.stringForKey(Keys.MessageAudioURL);
    }

    @Override
    public String getText() {
        return ChatSDK.audioMessage().toString(message);
    }

    public String getTotalTime() {
        int time = message.integerForKey(Keys.MessageAudioLength);
        int seconds = time%60;
        int minutes = (int) Math.floor(time/60f);
        return String.format(CurrentLocale.get(), "%01d:%02d", minutes, seconds);
    }
}
