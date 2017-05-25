package co.chatsdk.core.base;

import android.graphics.Bitmap;

import co.chatsdk.core.NetworkManager;
import co.chatsdk.core.dao.core.DaoCore;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.ImageUploadResult;
import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import co.chatsdk.core.utils.volley.ImageUtils;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public abstract class BaseUploadHandler implements UploadHandler {

    public Observable<ImageUploadResult> uploadImage(final Bitmap image, final Bitmap thumbnail) {

        if(image == null || thumbnail == null) {
            return Observable.error(new Throwable("The image and thumbnail can't be null"));
        }

        // Upload the two images in parallel
        Observable<FileUploadResult> o1 = NetworkManager.shared().a.upload.uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
        Observable<FileUploadResult> o2 = NetworkManager.shared().a.upload.uploadFile(ImageUtils.getImageByteArray(thumbnail), "thumbnail.jpg", "image/jpeg");

        return Observable.zip(o1, o2, new BiFunction<FileUploadResult, FileUploadResult, ImageUploadResult>() {
            @Override
            public ImageUploadResult apply(FileUploadResult s1, FileUploadResult s2) throws Exception {
                String imageURL = null, thumbnailURL = null;

                if (s1.name.equals("image.jpg")) {
                    imageURL = s1.url;
                }
                if (s2.name.equals("image.jpg")) {
                    imageURL = s2.url;
                }
                if (s1.name.equals("thumbnail.jpg")) {
                    thumbnailURL = s1.url;
                }
                if (s2.name.equals("thumbnail.jpg")) {
                    thumbnailURL = s2.url;
                }

                ImageUploadResult p = new ImageUploadResult(imageURL, thumbnailURL);
                p.progress = s1.progress.add(s2.progress);

                return p;
            }
        });
    }

    public Observable<FileUploadResult> uploadImage(final Bitmap image) {

        if(image == null) return Observable.error(new Throwable("Image can not be null"));

        return NetworkManager.shared().a.upload.uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
    }

    public String getUUID() {
        return DaoCore.generateRandomName();
    }

}
