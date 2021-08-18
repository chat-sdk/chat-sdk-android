package sdk.chat.core.rigs;

import android.graphics.Bitmap;

import java.io.File;

import sdk.chat.core.session.ChatSDK;

public class JPEGUploadable extends FileUploadable {

    public JPEGUploadable(File file, String name) {
        super(file, name, "image/jpeg", uploadable -> {
            if (uploadable instanceof JPEGUploadable) {
                JPEGUploadable jpegUploadable = (JPEGUploadable) uploadable;
                jpegUploadable.file = new id.zelory.compressor.Compressor(ChatSDK.ctx())
                        .setCompressFormat(Bitmap.CompressFormat.JPEG)
                        .setMaxHeight(ChatSDK.config().imageMaxHeight)
                        .setMaxWidth(ChatSDK.config().imageMaxWidth)
                        .compressToFile(jpegUploadable.file);
                return jpegUploadable;
            }
            return uploadable;
        });
    }
}
