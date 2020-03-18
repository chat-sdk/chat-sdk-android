package co.chatsdk.ui.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.image.ImageUtils;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.chat.MediaSelector;
import id.zelory.compressor.Compressor;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class ImagePickerUploader {

    protected MediaSelector.CropType cropType;
    protected MediaSelector mediaSelector = new MediaSelector();

    public static class Result {

        public String url;
        public String uri;

        public Result(String url, String uri) {
            this.url = url;
            this.uri = uri;
        }
    }

    public ImagePickerUploader(MediaSelector.CropType cropType) {
        this.cropType = cropType;
    }

    public Single<List<Result>> choosePhoto (Activity activity, boolean multiSelectEnabled) {
        return choosePhoto(activity, multiSelectEnabled, 0, 0);
    }

    public Single<List<Result>> choosePhoto (Activity activity, boolean multiSelectEnabled, int width, int height) {
        return PermissionRequestHandler.requestReadExternalStorage(activity)
                .andThen(mediaSelector.startChooseMediaActivity(activity, MimeType.ofImage(), cropType, multiSelectEnabled, width, height)
                        .flatMap(this::uploadImageFiles));
    }

    public Single<List<File>> choosePhoto(Activity activity) {
        return PermissionRequestHandler.requestReadExternalStorage(activity)
                .andThen(mediaSelector.startChooseMediaActivity(activity, MimeType.ofImage(), cropType, false));
    }

    public Single<List<Result>> uploadImageFiles (List<File> files) {
        return Single.defer(() -> {
            ArrayList<Result> results = new ArrayList<>();
            ArrayList<Single<Result>> singles = new ArrayList<>();

            for (File file: files) {
                singles.add(uploadImageFile(file));
            }

            return Single.concat(singles).doOnNext(results::add).ignoreElements().toSingle((Callable<List<Result>>) () -> results);
        });
    }

    public Single<Result> uploadImageFile(File file) {
        return Single.create((SingleOnSubscribe<Bitmap>) emitter -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            final Bitmap image = BitmapFactory.decodeFile(file.getPath(), options);
            emitter.onSuccess(image);
        }).flatMapObservable(ChatSDK.upload()::uploadImage).flatMapMaybe(fileUploadResult -> {
            if (fileUploadResult.urlValid()) {
                return Maybe.just(new Result(fileUploadResult.url, file.getPath()));
            } else {
                return Maybe.empty();
            }
        }).firstElement().toSingle().subscribeOn(Schedulers.io());
    }

}
