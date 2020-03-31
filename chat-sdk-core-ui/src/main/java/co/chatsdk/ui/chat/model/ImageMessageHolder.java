package co.chatsdk.ui.chat.model;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.ui.R;

public class ImageMessageHolder extends MessageHolder implements MessageContentType {

    public ImageMessageHolder(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        if (message.getMessageType().is(MessageType.Image)) {
            return ChatSDK.shared().getString(R.string.image_message);
        }
        if (message.getMessageType().is(MessageType.Location)) {
            return ChatSDK.shared().getString(R.string.location_message);
        }
        return super.getText();
    }


    @Nullable
    public String getImageUrl() {
        if (message.getMessageType().is(MessageType.Image, MessageType.Location, MessageType.Video)) {
            return message.stringForKey(Keys.MessageImageURL);
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
