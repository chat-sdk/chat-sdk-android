package sdk.chat.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import sdk.chat.core.R;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

/**
 * Created by ben on 10/24/17.
 */

public class BaseImageMessageHandler implements ImageMessageHandler {

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Image), thread, message -> {
            // Get the image and set the image text dimensions
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

            message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
            message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);

        }).setUploadable(new FileUploadable(imageFile, "image.jpg", "image/jpeg", uploadable -> {
            if (uploadable instanceof FileUploadable) {
                FileUploadable fileUploadable = (FileUploadable) uploadable;
                fileUploadable.file = new Compressor(ChatSDK.ctx())
                        .setMaxHeight(ChatSDK.config().imageMaxHeight)
                        .setMaxWidth(ChatSDK.config().imageMaxWidth)
                        .compressToFile(fileUploadable.file);
                return fileUploadable;
            }
            return uploadable;
        }), (message, result) -> {
            // When the file has uploaded, set the image URL
            message.setValueForKey(result.url, Keys.MessageImageURL);

        }).run();
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
