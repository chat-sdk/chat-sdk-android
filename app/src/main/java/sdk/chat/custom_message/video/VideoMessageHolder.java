package sdk.chat.custom_message.video;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import co.chatsdk.core.dao.Message;
import co.chatsdk.ui.chat.model.MessageHolder;

public class VideoMessageHolder extends MessageHolder implements MessageContentType {
    public VideoMessageHolder(Message message) {
        super(message);
    }
    public String getImageUrl() {
        return message.imageURL();
    }

}
