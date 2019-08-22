package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.theartofdev.edmodo.cropper.CropImage;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.options.MediaType;
import co.chatsdk.ui.utils.Cropper;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.disposables.Disposable;

import static android.app.Activity.RESULT_OK;

/**
 * Created by benjaminsmiley-andrews on 23/05/2017.
 */

public class MediaSelector {

    public static final int TAKE_PHOTO = 100;
    public static final int CHOOSE_PHOTO = 101;
    public static final int TAKE_VIDEO = 102;
    public static final int CHOOSE_VIDEO = 103;

    protected Uri fileUri;
    protected Disposable disposable;
    protected SingleEmitter<File> emitter;

    protected CropType cropType = CropType.Rectangle;

    public enum CropType {
        None,
        Rectangle,
        Square,
        Circle,
    }

    public Single<File> startActivity (Activity activity, MediaType type) {
        return startActivity(activity, type, null);
    }

    public Single<File> startActivity (Activity activity, MediaType type, CropType cropType) {

        if (cropType != null) {
            this.cropType = cropType;
        }

        Single<File> action = null;
        if (type.isEqual(MediaType.TakePhoto)) {
            action = startTakePhotoActivity(activity, this.cropType);
        }
        if (type.isEqual(MediaType.ChoosePhoto)) {
            action = startChooseImageActivity(activity, this.cropType);
        }
        if (type.isEqual(MediaType.TakeVideo)) {
            action = startTakeVideoActivity(activity);
        }
        if (type.isEqual(MediaType.ChooseVideo)) {
            action = startChooseVideoActivity(activity);
        }

        if (action != null) {
            if(type.is(MediaType.Take)) {
                return PermissionRequestHandler.shared().requestCameraAccess(activity).andThen(action);
            }
            if(type.is(MediaType.Choose)) {
                return PermissionRequestHandler.shared().requestReadExternalStorage(activity).andThen(action);
            }
        }
        return Single.error(new Throwable(activity.getString(R.string.error_launching_activity)));
    }

    public Single<File> startTakePhotoActivity (Activity activity, CropType cropType) {
        return Single.create(emitter -> {
            MediaSelector.this.emitter = emitter;
            MediaSelector.this.cropType = cropType;

            Context context = ChatSDK.shared().context();
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File destination = ImageUtils.createEmptyFileInCacheDirectory(context, "CAPTURE", ".jpg");
            fileUri = PhotoProvider.getPhotoUri(destination, context);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            if (!startActivityForResult(activity, intent, TAKE_PHOTO)) {
                notifyError(new Exception(activity.getString(R.string.unable_to_fetch_image)));
            }
        });
    }

    public Single<File> startChooseImageActivity(Activity activity, CropType cropType) {
        return Single.create(emitter -> {
            MediaSelector.this.emitter = emitter;
            MediaSelector.this.cropType = cropType;

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if(!startActivityForResult(activity, intent, CHOOSE_PHOTO)) {
                notifyError(new Exception(activity.getString(R.string.unable_to_start_activity)));
            };
        });
    }

    public Single<File> startTakeVideoActivity (Activity activity) {
        return Single.create(emitter -> {
            MediaSelector.this.emitter = emitter;

            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (!startActivityForResult(activity, intent, TAKE_VIDEO)) {
                notifyError(new Exception(activity.getString(R.string.unable_to_start_activity)));
            }
        });
    }

    public Single<File> startChooseVideoActivity (Activity activity) {
        return Single.create(emitter -> {
            MediaSelector.this.emitter = emitter;

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            if (!startActivityForResult(activity, intent, CHOOSE_VIDEO)) {
                notifyError(new Exception(activity.getString(R.string.unable_to_start_activity)));
            }
        });
    }

    protected boolean startActivityForResult (Activity activity, Intent intent, int tag) {
        if (disposable == null && intent.resolveActivity(activity.getPackageManager()) != null) {
            disposable = ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> handleResult(activity, activityResult.requestCode, activityResult.resultCode, activityResult.data));
            activity.startActivityForResult(intent, tag);
            return true;
        } else {
            return false;
        }
    }

    protected void processPickedPhoto(Activity activity, Uri uri) throws Exception {
        if (!ChatSDK.config().imageCroppingEnabled && cropType == CropType.None) {
            File imageFile = fileFromURI(uri, activity, MediaStore.Images.Media.DATA);
            // New
            File file = ImageUtils.compressImageToFile(activity, imageFile.getPath(), "COMPRESSED", "jpg");
            handleImageFile(activity, file);
        }
        else {
            if (cropType == CropType.Circle) {
                Cropper.startCircleActivity(activity, uri);
            }
            else if (cropType == CropType.Square) {
                Cropper.startSquareActivity(activity, uri);
            }
            else {
                Cropper.startActivity(activity, uri);
            }
        }
    }

    public static File fileFromURI (Uri uri, Activity activity, String column) {
        File file = null;
        if (uri.getPath() != null) {
            file = new File(uri.getPath());
        }
        if (file != null && file.length() > 0) {
            return file;
        }
        else {
            // Try to get it another way for this kind of URL
            // content://media/external ...
            String [] filePathColumn = { column };
            Cursor cursor = activity.getContentResolver().query(uri, filePathColumn,null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String fileURI = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                return new File(fileURI);
            }
        }
        return null;
    }


    protected void processCroppedPhoto(Activity activity, int resultCode, Intent data) throws Exception {

        CropImage.ActivityResult result = CropImage.getActivityResult(data);

        if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE || resultCode == Activity.RESULT_CANCELED) {
            throw new Exception(result.getError());
        }
        else if (resultCode == RESULT_OK) {
            try {
                handleImageFile(activity, new File(result.getUri().getPath()));
            }
            catch (NullPointerException e){
                notifyError(new Exception(activity.getString(R.string.unable_to_fetch_image)));
            }
        }

    }

    public void handleImageFile (Activity activity, File file) {

        // Scanning the messageImageView so it would be visible in the gallery images.
        if (ChatSDK.config().saveImagesToDirectory) {
            ImageUtils.scanFilePathForGallery(activity, file.getPath());
        }
        notifySuccess(file);
    }

    public void handleResult (Activity activity, int requestCode, int resultCode, Intent intent) throws Exception {

        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO) {
                processPickedPhoto(activity, intent.getData());
            }
            else if (requestCode == TAKE_PHOTO && fileUri != null) {
                processPickedPhoto(activity, fileUri);
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                processCroppedPhoto(activity, resultCode, intent);
            }
            else if (requestCode == TAKE_VIDEO || requestCode == CHOOSE_VIDEO) {
                Uri videoUri = intent.getData();
                notifySuccess(fileFromURI(videoUri, activity, MediaStore.Video.Media.DATA));
            }
            else {
                notifyError(new Exception(activity.getString(R.string.error_processing_image)));
            }
        } else {
            notifyError(new Exception(""));
        }
    }

    protected void notifySuccess (@NotNull File file) {
        if (emitter != null) {
            emitter.onSuccess(file);
        }
        clear();
    }

    protected void notifyError (@NotNull Throwable throwable) {
        if (emitter != null) {
            emitter.onError(throwable);
        }
        clear();
    }

    public void clear () {
        emitter = null;
        disposable.dispose();
        disposable = null;
    }
}
