package co.chatsdk.ui.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.image.ImageUploadResult;
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

    public ImagePickerUploader(MediaSelector.CropType cropType) {
        this.cropType = cropType;
    }

    public Single<List<ImageUploadResult>> choosePhoto (Activity activity, boolean multiSelectEnabled) {
        return choosePhoto(activity, multiSelectEnabled, 0, 0);
    }

    public Single<List<ImageUploadResult>> choosePhoto (Activity activity, boolean multiSelectEnabled, int width, int height) {
        return PermissionRequestHandler.requestReadExternalStorage(activity)
                .andThen(mediaSelector.startChooseMediaActivity(activity, MimeType.ofImage(), cropType, multiSelectEnabled, width, height)
                        .flatMap(this::uploadImageFiles));
    }

    public Single<List<File>> choosePhoto(Activity activity) {
        return PermissionRequestHandler.requestReadExternalStorage(activity)
                .andThen(mediaSelector.startChooseMediaActivity(activity, MimeType.ofImage(), cropType, false));
    }

    public Single<List<ImageUploadResult>> uploadImageFiles (List<File> files) {
        return Single.defer(() -> {
            ArrayList<ImageUploadResult> results = new ArrayList<>();
            ArrayList<Single<ImageUploadResult>> singles = new ArrayList<>();

            for (File file: files) {
                singles.add(ImageUtils.uploadImageFile(file));
            }

            return Single.concat(singles).doOnNext(results::add).ignoreElements().toSingle((Callable<List<ImageUploadResult>>) () -> results);
        });
    }


}
