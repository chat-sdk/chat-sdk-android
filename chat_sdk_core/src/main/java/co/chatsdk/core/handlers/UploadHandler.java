package co.chatsdk.core.handlers;

import android.graphics.Bitmap;

import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface UploadHandler {

    public Observable<FileUploadResult> uploadFile(byte[] data, String name, String mimeType);
    public Observable<ImageUploadResult> uploadImage(final Bitmap image, final Bitmap thumbnail);
    public Observable<FileUploadResult> uploadImage(final Bitmap image);

}
