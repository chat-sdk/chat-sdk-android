package co.chatsdk.core.base;

import android.graphics.Bitmap;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.handlers.UploadHandler;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageUploadResult;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.BiFunction;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.functions.Consumer;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public abstract class BaseUploadHandler implements UploadHandler {

    public Observable<MessageUploadResult> uploadImage(final Bitmap image) {
        return Observable.create(new ObservableOnSubscribe<MessageUploadResult>() {
            @Override
            public void subscribe(final ObservableEmitter<MessageUploadResult> e) throws Exception {
                if(image == null) {
                    e.onError(new Throwable("The image and thumbnail can't be null"));
                }
                else {
                    NM.upload().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg").subscribe(new Consumer<FileUploadResult>() {
                        @Override
                        public void accept(FileUploadResult fileUploadResult) throws Exception {
                            MessageUploadResult p = new MessageUploadResult(fileUploadResult.url, fileUploadResult.url);
                            e.onNext(p);
                        }
                    });
                }
            }
        });

//        // Upload the two images in parallel
//        Observable<FileUploadResult> o1 = NM.upload().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
//        Observable<FileUploadResult> o2 = NM.upload().uploadFile(ImageUtils.getImageByteArray(thumbnail), "thumbnail.jpg", "image/jpeg");
//
//        return Observable.zip(o1, o2, new BiFunction<FileUploadResult, FileUploadResult, MessageUploadResult>() {
//            @Override
//            public MessageUploadResult apply(FileUploadResult s1, FileUploadResult s2) throws Exception {
//                String imageURL = null, thumbnailURL = null;
//
//                if (s1.name != null && s1.name.equals("image.jpg")) {
//                    imageURL = s1.url;
//                }
//                if (s2.name != null && s2.name.equals("image.jpg")) {
//                    imageURL = s2.url;
//                }
//                if (s1.name != null && s1.name.equals("thumbnail.jpg")) {
//                    thumbnailURL = s1.url;
//                }
//                if (s2.name != null && s2.name.equals("thumbnail.jpg")) {
//                    thumbnailURL = s2.url;
//                }
//
//                MessageUploadResult p = new MessageUploadResult(imageURL, thumbnailURL);
//                p.progress = s1.progress.add(s2.progress);
//
//                return p;
//            }
//        });
    }

//    public Observable<FileUploadResult> uploadImage(final Bitmap image) {
//
//        if(image == null) return Observable.error(new Throwable("Image can not be null"));
//
//        return NM.upload().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg");
//    }

    public String getUUID() {
        return DaoCore.generateRandomName();
    }

}
