package sdk.chat.core.rigs;

import android.graphics.Bitmap;

import sdk.chat.core.image.ImageUtils;

public class BitmapUploadable extends Uploadable {

    Bitmap bitmap;

    public BitmapUploadable(Bitmap bitmap, String name, String mimeType, String messageKey) {
        this(bitmap, name, mimeType, messageKey, null);
    }

    public BitmapUploadable(Bitmap bitmap, String name, String mimeType, String messageKey, Compressor compressor) {
        super(name, mimeType, messageKey, compressor);
        this.bitmap = bitmap;
    }

    @Override
    public byte[] getBytes() {
        return ImageUtils.getImageByteArray(bitmap);
    }
}
