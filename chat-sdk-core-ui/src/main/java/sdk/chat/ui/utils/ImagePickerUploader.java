package sdk.chat.ui.utils;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.lassi.common.utils.KeyUtils;
import com.lassi.data.media.MiMedia;
import com.lassi.presentation.cropper.CropImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import sdk.chat.core.image.ImageUploadResult;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.activities.preview.LassiLauncher;
import sdk.guru.common.RX;

public class ImagePickerUploader {

    public interface Result {
        void onResult(ActivityResult result);
    }

    public static class Contract {

        WeakReference<Activity> activity;
        ActivityResultLauncher<Intent> launcher;
        Result result;

        public Contract(Activity activity) {
            this.activity = new WeakReference<>(activity);
        }

        public void setLauncher(ActivityResultLauncher<Intent> launcher) {
            this.launcher = launcher;
        }

        public ActivityResultLauncher<Intent> getLauncher() {
            return launcher;
        }

        public void setActivityResult(ActivityResult result) {
            if (this.result != null) {
                this.result.onResult(result);
            }
        }

        public void setResultListener(Result resultListener) {
            this.result = resultListener;
        }

        @NonNull public Activity getActivity() {
            return activity.get();
        }
    }

    public Single<List<ImageUploadResult>> chooseCircularPhoto(Contract contract, int size) {
        return choosePhoto(contract, 1, null, true, size, size);
    }

    public Single<List<ImageUploadResult>> chooseCircularPhoto(Contract contract) {
        return chooseCircularPhoto(contract, ChatSDK.config().imageMaxWidth);
    }

    public Single<List<ImageUploadResult>> choosePhoto(Contract contract, boolean multiSelectEnabled) {
        return choosePhoto(contract, multiSelectEnabled ? 8 : 1, CropImageView.CropShape.RECTANGLE, false, 0, 0);
    }

    public Single<List<ImageUploadResult>> choosePhoto(Contract contract, int maxSelect, CropImageView.CropShape crop, boolean isCircle, int width, int height) {
        return PermissionRequestHandler.requestImageMessage(contract.getActivity())
                .andThen(Single.create((SingleOnSubscribe<List<File>>) emitter -> {
                    Intent intent = LassiLauncher.Companion.launchImagePicker(contract.getActivity(), maxSelect, crop, isCircle);
                    contract.setResultListener(activityResult -> {
                        RX.io().scheduleDirect(() -> {
                            List<File> files = new ArrayList<>();
                            if (activityResult.getResultCode() == Activity.RESULT_OK) {
                                if (activityResult.getData() != null) {
                                    List<MiMedia> selectedMedia = (List<MiMedia>) activityResult.getData().getSerializableExtra(KeyUtils.SELECTED_MEDIA);
                                    if (selectedMedia != null && !selectedMedia.isEmpty()) {
                                        for(MiMedia media: selectedMedia) {
                                            try {
                                                File file = Glide.with(contract.getActivity()).asFile().load(new File(media.getPath())).submit().get();
//                                                Bitmap image = BitmapFactory.decodeFile(file.getPath());
//                                            Bitmap scaled = ImageUtils.scaleImage(image, Math.max(ChatSDK.config().imageMaxWidth, ChatSDK.config().imageMaxHeight));

//                                                File compressed = new Compressor(ChatSDK.ctx())
//                                                        .setMaxWidth(width)
//                                                        .setMaxHeight(height)
//                                                        .compressToFile(file);

                                                files.add(file);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                            emitter.onSuccess(files);
                        });
                    });
                    contract.getLauncher().launch(intent);
                }).subscribeOn(RX.io())).flatMap(this::uploadImageFiles);
    }

    public Single<List<ImageUploadResult>> uploadImageFiles(List<File> files) {
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
