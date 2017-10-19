package co.chatsdk.core.base;

import android.graphics.Bitmap;

import co.chatsdk.core.session.NM;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.types.FileUploadResult;
import io.reactivex.Observable;
import co.chatsdk.core.utils.ImageUtils;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public abstract class AbstractUploadHandler implements UploadHandler {

    public Observable<FileUploadResult> uploadImage(final Bitmap image) {
        return NM.upload().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
    }

    public String getUUID() {
        return DaoCore.generateRandomName();
    }

}
