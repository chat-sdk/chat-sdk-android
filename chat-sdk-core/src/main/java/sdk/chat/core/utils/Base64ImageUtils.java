package sdk.chat.core.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import sdk.chat.core.image.ImageUtils;

public class Base64ImageUtils {

    /**
     * @param bitmap image to convert to base64
     * @param width max width
     * @param jpegQuality 0 - 100, 0 -> lowest quality, 100-> highest quality
     */
    public static String toBase64(Bitmap bitmap, int width, int jpegQuality) {

        int height = Math.round((float) bitmap.getHeight() * (float) width / (float) bitmap.getWidth());
        Bitmap scaled = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        scaled = scaled.copy(Bitmap.Config.RGB_565, false);

        // Convert to JPEG
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, jpegQuality, out);

        return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);

    }

    public static Bitmap fromBase64(String base64) {
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

}
