package sdk.chat.ui.chat.model;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

public class ImageMessageHolder extends MessageHolder implements MessageContentType {

    public ImageMessageHolder(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        if (message.getMessageType().is(MessageType.Image)) {
            return ChatSDK.imageMessage().toString(message);
        }
        if (message.getMessageType().is(MessageType.Location)) {
            return ChatSDK.locationMessage().toString(message);
        }
        return super.getText();
    }


    @Nullable
    public String getImageUrl() {
        if (message.getMessageType().is(MessageType.Image, MessageType.Location, MessageType.Video)) {
            return message.getImageURL();
        }
        return null;
    }

    public Integer width() {
        Object width = message.valueForKey(Keys.MessageImageWidth);
        if (width instanceof Integer) {
            return (Integer) width;
        }
        return null;
    }

    public Integer height() {
        Object height = message.valueForKey(Keys.MessageImageHeight);
        if (height instanceof Integer) {
            return (Integer) height;
        }
        return null;
    }

}
