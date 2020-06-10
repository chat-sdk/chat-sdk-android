package sdk.chat.core.base;

import android.graphics.Bitmap;

import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.handlers.UploadHandler;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.FileUploadResult;
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
