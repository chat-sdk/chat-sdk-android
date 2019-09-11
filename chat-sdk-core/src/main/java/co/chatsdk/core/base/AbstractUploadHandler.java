package co.chatsdk.core.base;

import android.graphics.Bitmap;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.Observable;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public abstract class AbstractUploadHandler implements UploadHandler {

    public Observable<FileUploadResult> uploadImage(final Bitmap image) {
        return ChatSDK.upload().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
    }

    public String getUUID() {
        return DaoCore.generateRandomName();
    }

    public boolean shouldUploadAvatar () {
        return false;
    }

}
