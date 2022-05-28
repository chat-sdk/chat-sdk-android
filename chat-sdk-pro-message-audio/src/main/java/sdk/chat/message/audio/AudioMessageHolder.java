package sdk.chat.message.audio;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.ui.chat.model.MessageHolder;

public class AudioMessageHolder extends MessageHolder implements MessageContentType {

    public AudioMessageHolder(Message message) {
        super(message);
    }

    public String audioURL() {
        if (AudioMessageModule.config().preferStreamAudio) {
            return message.stringForKey(Keys.MessageAudioURL);
        }
        else {
            return message.getFilePath();
        }
    }

    public String getTotalTime() {
        int time = message.integerForKey(Keys.MessageAudioLength);
        int seconds = time%60;
        int minutes = (int) Math.floor(time/60f);
        return String.format(CurrentLocale.get(), "%01d:%02d", minutes, seconds);
    }
}
