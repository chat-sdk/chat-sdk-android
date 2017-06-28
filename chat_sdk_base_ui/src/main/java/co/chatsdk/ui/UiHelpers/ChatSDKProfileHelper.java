/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.UiHelpers;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.ImageUploadResult;
import co.chatsdk.core.utils.ImageUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Completable;
import io.reactivex.functions.Function;
import co.chatsdk.ui.R;

import co.chatsdk.ui.utils.ChatSDKIntentClickListener;
import com.braunster.chatsdk.object.Cropper;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

import timber.log.Timber;

// TODO: Refactor this class
public class ChatSDKProfileHelper {

    public static final int ERROR = 1991, NOT_HANDLED = 1992, HANDLED = 1993;

    private static final String TAG = ChatSDKProfileHelper.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Cropper crop;

    private Fragment fragment = null;

    private static final int PROFILE_PIC = 100;

    private CircleImageView profileCircleImageView;
    public ProgressBar progressBar;

    private AppCompatActivity activity;

    private UIHelper uiHelper;

    private View mainView;
    private PostProfilePic postProfilePic;

    public ChatSDKProfileHelper(AppCompatActivity activity, CircleImageView profileCircleImageView, ProgressBar progressBar, UIHelper uiHelper, View mainView) {
        this.profileCircleImageView = profileCircleImageView;
        this.progressBar = progressBar;
        this.activity = activity;
        this.uiHelper = uiHelper;
        this.mainView = mainView;
    }

    private class PostProfilePic implements Runnable{

        private Bitmap bitmap;

        private PostProfilePic(Bitmap bitmap){
            this.bitmap = bitmap;
        }

        @Override
        public void run() {
            int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();
            profileCircleImageView.setImageBitmap(scaleImage(bitmap, size));
            progressBar.setVisibility(View.GONE);
            profileCircleImageView.setVisibility(View.VISIBLE);
        }
    }

    public void setProfilePic(final Bitmap bitmap){
        // load image into imageview
        final int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();

        if (postProfilePic != null)
            profileCircleImageView.removeCallbacks(postProfilePic);

        // If the size of the container is 0 we will wait for the view to do onLayout and only then measure it.
        if (size == 0)
        {
            postProfilePic = new PostProfilePic(bitmap);

            profileCircleImageView.post(postProfilePic);
        } else
        {
            profileCircleImageView.setImageBitmap(scaleImage(bitmap, size));
            progressBar.setVisibility(View.GONE);
            profileCircleImageView.setVisibility(View.VISIBLE);
        }
    }

    /** Only for current user.*/
    private Completable saveProfilePicToServer(String path){
        Bitmap image = ImageUtils.getCompressed(path);

        Bitmap thumbnail = ImageUtils.getCompressed(path,
                Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE,
                Defines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);

        // TODO: Are we handling the error here
        return NM.upload().uploadImage(image, thumbnail).flatMapCompletable(new Function<ImageUploadResult, Completable>() {
            @Override
            public Completable apply(ImageUploadResult profileImageUploadResult) throws Exception {

                // Saving the image to backendless.
                final BUser currentUser = NM.currentUser();

                currentUser.setMetaPictureUrl(profileImageUploadResult.imageURL);
                currentUser.setMetaPictureThumbnail(profileImageUploadResult.thumbnailURL);

                NM.core().pushUser();

                return Completable.complete();
            }
        });
    }

    private void setInitialsProfilePic(boolean save){
        String initials = "";

        String name = NM.currentUser().getMetaName();

        if (StringUtils.isEmpty(name))
            initials = Defines.InitialsForAnonymous;
        else
        {
            String[] splited = name.split("\\s+");
            if (splited.length == 1)
                initials = String.valueOf(name.toUpperCase().charAt(0));
            else if (splited.length >= 2)
                initials = String.valueOf(splited[0].toUpperCase().charAt(0)) + String.valueOf(splited[1].toUpperCase().charAt(0));
            else initials = Defines.InitialsForAnonymous;
        }

        setInitialsProfilePic(initials, save);
    }

    private void setInitialsProfilePic(final String initials, boolean save) {
        Bitmap bitmap = ImageUtils.getInitialsBitmap(Color.GRAY, Color.BLACK, initials);
        setProfilePic(bitmap);

        if (save)
            createTempFileAndSave(bitmap);
    }

    private boolean createTempFileAndSave(Bitmap bitmap){
        // Saving the image to tmp file.
        try {
            File tmp = File.createTempFile("Pic", ".jpg", activity.getCacheDir());
            ImageUtils.saveBitmapToFile(tmp, bitmap);
            saveProfilePicToServer(tmp.getPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp){
        if (boundBoxInDp == 0)
            return null;

        // Get current dimensions
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) boundBoxInDp) / width;
        float yScale = ((float) boundBoxInDp) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;

        // Create a matrix for the scaling and add the scaling bundle
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return bitmap;
    }

    public int handleResult(int requestCode, int resultCode, Intent data){
        if (data == null) {
            return NOT_HANDLED;
        }

        if (requestCode == PROFILE_PIC)
        {
            if (resultCode == AppCompatActivity.RESULT_OK)
            {
                Uri uri = data.getData();

                Uri outputUri = Uri.fromFile(new File(this.activity.getCacheDir(), "cropped.jpg"));
                crop = new Cropper(uri);

                Intent cropIntent = crop.getIntent(this.activity, outputUri);
                int request =Crop.REQUEST_CROP + PROFILE_PIC;

                if (fragment==null)
                    activity.startActivityForResult(cropIntent, request);
                else activity.startActivityFromFragment(fragment, cropIntent, request);

                return HANDLED;
            }
        }
        else  if (requestCode == Crop.REQUEST_CROP + PROFILE_PIC) {
            if (resultCode == Crop.RESULT_ERROR)
            {
                return ERROR;
            }

            try
            {
                File image = new File(this.activity.getCacheDir(), "cropped.jpg");
                saveProfilePicToServer(image.getPath()).subscribe();

                return HANDLED;
            }
            catch (NullPointerException e){
                if (DEBUG) Timber.e("Null pointer when getting file.");
                uiHelper.showToast(R.string.unable_to_fetch_image);
                return ERROR;
            }
        }

        return NOT_HANDLED;
    }

    public void initViews(){
        if (mainView!=null)
        {
            profileCircleImageView = (CircleImageView) mainView.findViewById(R.id.chat_sdk_circle_ing_profile_pic);
            progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);
        }
    }

    public static View.OnClickListener getProfilePicClickListener(final AppCompatActivity activity, final Fragment fragment){
        return ChatSDKIntentClickListener.getPickImageClickListener(activity, fragment, PROFILE_PIC);
    }

    /** If set the helper will use this fragment when calling startActivityForResult*/
    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

}
