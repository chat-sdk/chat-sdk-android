package co.chatsdk.core.handlers;

import android.graphics.Bitmap;

import co.chatsdk.core.types.FileUploadResult;
import io.reactivex.Observable;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface UploadHandler {

    Observable<FileUploadResult> uploadFile(byte[] data, String name, String mimeType);
//    Observable<MessageUploadResult> uploadImage(final Bitmap image, final Bitmap thumbnail);
    Observable<FileUploadResult> uploadImage(final Bitmap image);

}
