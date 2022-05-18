package sdk.chat.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.manager.Base64ImageMessagePayload;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Base64ImageUtils;

public class Base64ImageMessageHandler extends BaseImageMessageHandler {

    public int width = 800;
    public int jpegQuality = 20;

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {
        MessageSendRig rig = new MessageSendRig(new MessageType(MessageType.Base64Image), thread, message -> {

            // Get the image and set the image text dimensions
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

            int height = Math.round((float) image.getHeight() * (float) this.width / (float) image.getWidth());

            String encoded = Base64ImageUtils.toBase64(image, width, jpegQuality);

            message.setValueForKey(encoded, Keys.MessageImageData);
            message.setValueForKey(this.width, Keys.MessageImageWidth);
            message.setValueForKey(height, Keys.MessageImageHeight);

        });
        return rig.run();
    }

    @Override
    public MessagePayload payloadFor(Message message) {
        if (message.typeIs(MessageType.Image)) {
            return new Base64ImageMessagePayload(message);
        }
        return null;
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.Base64Image);
    }

}
