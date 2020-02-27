package sdk.chat.custom_message.audio;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import co.chatsdk.core.audio.AudioPlayer;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.ui.chat.model.MessageHolder;

public class AudioMessageHolder extends MessageHolder implements MessageContentType {

    public AudioMessageHolder(Message message) {
        super(message);
    }

    public String audioURL() {
        return message.stringForKey(Keys.MessageAudioURL);
    }

    public String length() {
        Object audioMessageLength = message.valueForKey(Keys.MessageAudioLength);
        double length = -1;

        if(audioMessageLength instanceof String) {
            // Convert it to a double so we can format it
            length = Double.parseDouble((String) audioMessageLength);
        }
        else if(audioMessageLength instanceof Integer) {
            length = ((Integer) audioMessageLength).doubleValue();
        }
        else if(audioMessageLength instanceof Double) {
            length = (Double) audioMessageLength;
        }
        if(length != -1) {
            return AudioPlayer.toSeconds((int) (Math.floor(length) * 1000));
        }
        return "0:00";
    }
}
