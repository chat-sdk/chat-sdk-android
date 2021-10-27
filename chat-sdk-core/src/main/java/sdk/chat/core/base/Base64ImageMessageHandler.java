package sdk.chat.core.base;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.types.MessageType;

public class Base64ImageMessageHandler extends BaseImageMessageHandler {

    public int width = 800;
    public int jpegQuality = 20;

    @Override
    public Completable sendMessageWithImage(final File imageFile, final Thread thread) {
        MessageSendRig rig = new MessageSendRig(new MessageType(MessageType.Base64Image), thread, message -> {

            // Get the image and set the image text dimensions
            final Bitmap image = BitmapFactory.decodeFile(imageFile.getPath(), null);

            int height = Math.round((float) image.getHeight() * (float) this.width / (float) image.getWidth());
            Bitmap scaled = Bitmap.createScaledBitmap(image, width, height, false);

            // Convert to JPEG
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, this.jpegQuality, out);

            String encoded = Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);

            message.setValueForKey(encoded, Keys.MessageImageData);
            message.setValueForKey(this.width, Keys.MessageImageWidth);
            message.setValueForKey(height, Keys.MessageImageHeight);

        });
        return rig.run();
    }

}
