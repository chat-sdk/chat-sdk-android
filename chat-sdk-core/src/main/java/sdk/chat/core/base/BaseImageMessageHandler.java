package sdk.chat.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import io.reactivex.Completable;
import sdk.chat.core.R;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.rigs.JPEGUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

/**
 * Created by ben on 10/24/17.
 */

public class BaseImageMessageHandler implements ImageMessageHandler {

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {

        MessageSendRig rig = new MessageSendRig(new MessageType(MessageType.Image), thread, message -> {
            // Get the image and set the image text dimensions
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

            message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
            message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);

        }).setUploadable(new JPEGUploadable(imageFile, "image.jpg", Keys.MessageImageURL), (message, result) -> {
            // When the file has uploaded, set the image URL
//            message.setValueForKey(result.url, Keys.MessageImageURL);

        });
        return rig.run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageImageURL);
    }

    @Override
    public String toString(Message message) {
        return ChatSDK.getString(R.string.image_message);
    }

    @Override
    public String getImageURL(Message message) {
        if (message.getMessageType().is(MessageType.Image) || message.getReplyType().is(MessageType.Image)) {
            return message.getImageURL();
        }
        return null;
    }

}
