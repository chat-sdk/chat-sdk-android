package sdk.chat.message.location;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.AbstractMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.StringChecker;

public class LocationMessagePayload extends AbstractMessagePayload {

    public LocationMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return String.format(
                ChatSDK.config().locationURLRepresentation,
                message.doubleForKey(Keys.MessageLatitude),
                message.doubleForKey(Keys.MessageLongitude)
        );
    }

    @Override
    public String imageURL() {
        if (message.getMessageType().is(MessageType.Location) || message.getReplyType().is(MessageType.Location)) {
            return message.getImageURL();
        }
        return null;
    }

    @Override
    public String toString() {
        String text = super.toString();
        if (StringChecker.isNullOrEmpty(text)) {
            text = ChatSDK.getString(sdk.chat.core.R.string.location_message);
        }
        return text;
    }
}
