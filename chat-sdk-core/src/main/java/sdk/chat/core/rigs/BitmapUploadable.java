package sdk.chat.core.rigs;

import android.graphics.Bitmap;

import sdk.chat.core.image.ImageUtils;

public class BitmapUploadable extends Uploadable {

    Bitmap bitmap;

    public BitmapUploadable(Bitmap bitmap, String name, String mimeType) {
        this(bitmap, name, mimeType, null);
    }

    public BitmapUploadable(Bitmap bitmap, String name, String mimeType, Compressor compressor) {
        super(name, mimeType, compressor);
        this.bitmap = bitmap;
    }

    @Override
    public byte[] getBytes() {
        return ImageUtils.getImageByteArray(bitmap);
    }
}
