package co.chatsdk.ui.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.utils.ActivityResult;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.DisposableMap;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.chat.MediaSelector;
import id.zelory.compressor.Compressor;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

public class ImagePickerUploader {

    protected MediaSelector.CropType cropType;
    protected MediaSelector mediaSelector = new MediaSelector();

    public enum Status {
        Uploading,
        Complete
    }

    public class Result {


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

//    public Single<Result> takePhoto (Activity activity) {
//        return mediaSelector.startTakePhotoActivity(activity, cropType).map(this::compressFile).flatMap(this::uploadImageFile);
//    }

    public Single<List<Result>> choosePhoto (Activity activity) {
//        return PermissionRequestHandler.requestReadExternalStorage(activity).toSingle(() -> {
//            return new Result(null, null);
//        });
//
//        final int requestCode = 141;
//        Single<Result> single = ActivityResultPushSubjectHolder.shared().filter(new Predicate<ActivityResult>() {
//            @Override
//            public boolean test(ActivityResult activityResult) throws Exception {
//                return activityResult.requestCode == requestCode;
//            }
//        }).firstOrError().map(new Function<ActivityResult, Result>() {
//            @Override
//            public Result apply(ActivityResult activityResult) throws Exception {
//                return new Result(null, null);
//            }
//        });
//
//        Matisse.from(activity).choose(MimeType.ofImage()).maxSelectable(1).imageEngine(new PicassoEngine()).forResult(requestCode);
//        return single;
        return mediaSelector.startChooseMediaActivity(activity, MimeType.ofImage(), cropType, false).map(this::compressFiles).flatMap(this::uploadImageFiles);
    }

    public List<File> compressFiles (List<File> files) throws Exception {
        ArrayList<File> compressedFiles = new ArrayList<>();

        for (File file: files) {
            File compress = new Compressor(ChatSDK.shared().context())
                    .setMaxHeight(ChatSDK.config().imageMaxThumbnailDimension)
                    .setMaxWidth(ChatSDK.config().imageMaxThumbnailDimension)
                    .compressToFile(file);
            Bitmap bitmap = BitmapFactory.decodeFile(compress.getPath());
            compressedFiles.add(ImageUtils.compressImageToFile(ChatSDK.shared().context(), bitmap, ChatSDK.currentUserID(), ".png"));
        }
        return compressedFiles;
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
        }).firstElement().toSingle();
    }

}
