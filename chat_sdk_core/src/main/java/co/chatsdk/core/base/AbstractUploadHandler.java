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
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import co.chatsdk.core.utils.ImageUtils;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by benjaminsmiley-andrews on 24/05/2017.
 */

public abstract class AbstractUploadHandler implements UploadHandler {

    public Observable<MessageUploadResult> uploadImage(final Bitmap image) {
        return Observable.create(new ObservableOnSubscribe<MessageUploadResult>() {
            @Override
            public void subscribe(final ObservableEmitter<MessageUploadResult> e) throws Exception {
                if(image == null) {
                    e.onError(new Throwable("The image and thumbnail can't be null"));
                }
                else {
                    NM.upload().uploadFile(ImageUtils.getImageByteArray(image), "image.jpg", "image/jpeg").subscribe(new Observer<FileUploadResult>() {
                        @Override
                        public void onSubscribe(Disposable d) {}

                        @Override
                        public void onNext(FileUploadResult value) {
                            MessageUploadResult p = new MessageUploadResult(value.url, value.url);
                            e.onNext(p);
                        }

                        @Override
                        public void onError(Throwable ex) {
                            e.onError(ex);
                        }

                        @Override
                        public void onComplete() {
                            e.onComplete();
                        }
                    });
                }
            }
        }).subscribeOn(Schedulers.single());

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
