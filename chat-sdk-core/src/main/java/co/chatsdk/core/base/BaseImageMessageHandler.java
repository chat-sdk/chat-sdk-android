package co.chatsdk.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.IOException;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.ImageMessageHandler;
import co.chatsdk.core.rigs.FileUploadable;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.rigs.Uploadable;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageType;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;

/**
 * Created by ben on 10/24/17.
 */

public class BaseImageMessageHandler implements ImageMessageHandler {

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Image), thread, message -> {
            // Get the image and set the image message dimensions
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

            message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
            message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);

        }).setUploadable(new FileUploadable(imageFile, "image.jpg", "image/jpeg", uploadable -> {
            if (uploadable instanceof FileUploadable) {
                FileUploadable fileUploadable = (FileUploadable) uploadable;
                fileUploadable.file = new Compressor(ChatSDK.shared().context())
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
}
