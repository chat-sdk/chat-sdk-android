package sdk.chat.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.pmw.tinylog.Logger;

import java.io.File;

import io.reactivex.Completable;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.ImageMessageHandler;
import sdk.chat.core.manager.ImageMessagePayload;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.JPEGUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Base64ImageUtils;
import sdk.guru.common.RX;

/**
 * Created by ben on 10/24/17.
 */

public class BaseImageMessageHandler extends AbstractMessageHandler implements ImageMessageHandler {

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {
        return Completable.defer(() -> {

            final Uploadable uploadable = new JPEGUploadable(imageFile, "image.jpg", Keys.MessageImageURL);

            MessageSendRig rig = new MessageSendRig(new MessageType(MessageType.Image), thread, message -> {

                // Get the image and set the image text dimensions
                final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

                // Get the cached file
                CachedFile file = ChatSDK.uploadManager().add(uploadable, message);

                // Add the placeholder
                message.setPlaceholderPath(file.getLocalPath());

                // Add a base64 preview
                if (ChatSDK.config().sendBase64ImagePreview) {
                    String base64 = Base64ImageUtils.toBase64(image, ChatSDK.config().imagePreviewMaxSize, ChatSDK.config().imagePreviewQuality);
                    message.setValueForKey(base64, Keys.MessageImagePreview);
                }

                message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
                message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);

            }).setUploadable(uploadable, (message, result) -> {
                // When the file has uploaded, set the image URL
//            message.setValueForKey(result.url, Keys.MessageImageURL);

            });

            Logger.debug("ImageSend: " + imageFile.getPath());

            return rig.run();
        }).subscribeOn(RX.computation());
    }

    @Override
    public MessagePayload payloadFor(Message message) {
        if (message.typeIs(MessageType.Image) || message.isReply() && message.getReplyType().is(MessageType.Image)) {
            return new ImageMessagePayload(message);
        }
        return null;
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.Image);
    }


}
