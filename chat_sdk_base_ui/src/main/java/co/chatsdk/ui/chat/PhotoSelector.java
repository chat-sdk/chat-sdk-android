package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import co.chatsdk.core.types.Defines;
import co.chatsdk.core.dao.DaoCore;

import com.braunster.chatsdk.object.Cropper;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import co.chatsdk.core.utils.volley.ImageUtils;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.Utils;

/**
 * Created by benjaminsmiley-andrews on 23/05/2017.
 */

public class PhotoSelector {

    public static final int CAPTURE_IMAGE = 101;
    public static final int PHOTO_PICKER_ID = 100;
    private String filePath;
    private Result resultHandler;

    public interface Result {
        void result (String result);
    }

    public void startTakePhotoActivity (Activity activity, Result resultHandler) throws Exception {
        this.resultHandler = resultHandler;

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file, dir = Utils.ImageSaver.getAlbumStorageDir(activity, Utils.ImageSaver.IMAGE_DIR_NAME);

        if (dir == null)
        {
            throw new Exception(activity.getString(R.string.unable_to_catch_image));
        }

        if(dir.exists())
        {
            file = new File(dir, DaoCore.generateRandomName() + ".jpg");
            filePath = file.getPath();
            //selectedFilePath = file.getPath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }

        // start the image capture Intent
        activity.startActivityForResult(intent, CAPTURE_IMAGE);

    }

    public void startPickImageActivity (Activity activity, Result resultHandler) {
        this.resultHandler = resultHandler;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        activity.startActivityForResult(Intent.createChooser(intent,"Complete action using"), PHOTO_PICKER_ID);

    }

    private void processPickedPhoto(Activity activity, int resultCode, Intent data) throws Exception {

        switch (resultCode)
        {
            case AppCompatActivity.RESULT_OK:

                Uri uri = data.getData();
                filePath = DaoCore.generateRandomName();

                // If enabled we will save the image to the app
                // directory in gallery else we will save it in the cache dir.
                File dir;
                if (Defines.Options.SaveImagesToDir)
                    dir = Utils.ImageSaver.getAlbumStorageDir(activity, Utils.ImageSaver.IMAGE_DIR_NAME);
                else
                    dir = activity.getCacheDir();

                if (dir == null)
                {
                    throw new Exception(activity.getString(R.string.unable_to_fetch_image));
                }

                Uri outputUri = Uri.fromFile(new File(dir, filePath + ".jpeg"));

                Cropper crop = new Cropper(uri);

                Intent cropIntent = crop.getAdjustIntent(activity, outputUri);
                int request = Crop.REQUEST_CROP + PHOTO_PICKER_ID;

                activity.startActivityForResult(cropIntent, request);

                break;
            case AppCompatActivity.RESULT_CANCELED:
                throw new Exception();
        }
    }

    private void processCroppedPhoto(Activity activity, int resultCode, Intent data) throws Exception {

        if (resultCode == Crop.RESULT_ERROR) {
            throw new Exception();
        }

        try {
            // If enabled we will save the image to the app
            // directory in gallery else we will save it in the cache dir.
            File dir;
            if (Defines.Options.SaveImagesToDir)
                dir = Utils.ImageSaver.getAlbumStorageDir(activity, Utils.ImageSaver.IMAGE_DIR_NAME);
            else
                dir = activity.getCacheDir();

            if (dir == null)
            {
                throw new Exception(activity.getString(R.string.unable_to_fetch_image));
            }

            File image = new File(dir, filePath + ".jpeg");

            String selectedFilePath = image.getPath();

            // Scanning the image so it would be visible in the gallery images.
            if (Defines.Options.SaveImagesToDir) {
                ImageUtils.scanFilePathForGallery(activity, selectedFilePath);
            }

            if(resultHandler != null) {
                resultHandler.result(image.getPath());
                clear();
            }
        }
        catch (NullPointerException e){
            throw new Exception(activity.getString(R.string.unable_to_fetch_image));
        }
    }

    public void handleResult (Activity activity, int requestCode, int resultCode, Intent data) throws Exception {
        if (requestCode == PHOTO_PICKER_ID)
        {
            processPickedPhoto(activity, resultCode, data);
        }
        else  if (requestCode == Crop.REQUEST_CROP + PHOTO_PICKER_ID) {
            processCroppedPhoto(activity, resultCode, data);
        }
        /* Capture image logic*/
        else if (requestCode == CAPTURE_IMAGE && resultCode == AppCompatActivity.RESULT_OK)
        {
            if(resultHandler != null) {
                resultHandler.result(filePath);
                clear();
            }
        }
    }

    public void clear () {
        filePath = null;
        resultHandler = null;
    }



}
