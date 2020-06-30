package sdk.chat.ui.utils;

import android.app.Activity;

import com.zhihu.matisse.MimeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import sdk.chat.core.api.SimpleAPI;
import sdk.chat.core.image.ImageUploadResult;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.chat.MediaSelector;
import io.reactivex.Single;

public class ImagePickerUploader {

    protected MediaSelector.CropType cropType;
    protected MediaSelector mediaSelector = new MediaSelector();

    public ImagePickerUploader(MediaSelector.CropType cropType) {
        this.cropType = cropType;
    }

    public Single<List<ImageUploadResult>> choosePhoto (Activity activity, boolean multiSelectEnabled) {
        return choosePhoto(activity, multiSelectEnabled, 0, 0);
    }

    public Single<List<ImageUploadResult>> choosePhoto(Activity activity, boolean multiSelectEnabled, int width, int height) {
        return PermissionRequestHandler.requestImageMessage(activity)
                .andThen(mediaSelector.startChooseMediaActivity(activity, MimeType.ofImage(), cropType, multiSelectEnabled, true, width, height)
                        .flatMap(this::uploadImageFiles));
    }

    public Single<List<File>> choosePhoto(Activity activity) {
        return PermissionRequestHandler.requestImageMessage(activity)
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
