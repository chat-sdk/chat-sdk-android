package sdk.chat.message.location;

import android.location.Location;

import io.reactivex.Completable;
import sdk.chat.core.base.AbstractMessageHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.LocationMessageHandler;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.GoogleUtils;

/**
 * Created by ben on 10/24/17.
 */

public class BaseLocationMessageHandler extends AbstractMessageHandler implements LocationMessageHandler {

    @Override
    public Completable sendMessageWithLocation(final String filePath, final Location location, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Location), thread, message -> {

            int maxSize = ChatSDK.config().imageMaxThumbnailDimension;
            String imageURL = GoogleUtils.getMapImageURL(location, maxSize, maxSize);

            message.setValueForKey(location.getLongitude(), Keys.MessageLongitude);
            message.setValueForKey(location.getLatitude(), Keys.MessageLatitude);
            message.setValueForKey(maxSize, Keys.MessageImageWidth);
            message.setValueForKey(maxSize, Keys.MessageImageHeight);
            message.setValueForKey(imageURL, Keys.MessageImageURL);

        }).run();
    }

    @Override
    public MessagePayload payloadFor(Message message) {
        return new LocationMessagePayload(message);
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.Location);
    }

}
