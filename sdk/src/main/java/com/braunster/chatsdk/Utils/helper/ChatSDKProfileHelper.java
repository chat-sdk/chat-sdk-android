/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.helper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Cropper;
import com.braunster.chatsdk.object.SaveImageProgress;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public class ChatSDKProfileHelper {

    public static final int ERROR = 1991, NOT_HANDLED = 1992, HANDELD = 1993;

    private static final String LAST_IMAGE_PATH = "last_image_path";

    private static final String TAG = ChatSDKProfileHelper.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Cropper crop;

    private Fragment fragment = null;

    /** If this was set this is the user that will be used instead of the current user.*/
    private BUser profileUser;

    public static final int PROFILE_PIC = 100;

    public CircleImageView profileCircleImageView;
    public ProgressBar progressBar;

    private Activity activity;

    private ChatSDKUiHelper uiHelper;

    private View mainView;

    public ChatSDKProfileHelper(Activity activity, ChatSDKUiHelper uiHelper, View mainView) {
        this.activity = activity;
        this.uiHelper = uiHelper;
        this.mainView = mainView;
    }

    public ChatSDKProfileHelper(Activity activity, CircleImageView profileCircleImageView, ProgressBar progressBar, ChatSDKUiHelper uiHelper, View mainView) {
        this.profileCircleImageView = profileCircleImageView;
        this.progressBar = progressBar;
        this.activity = activity;
        this.uiHelper = uiHelper;
        this.mainView = mainView;
    }

    private static String lastImageLoadedPath = "";

    private boolean saveImageWhenLoaded = true;

    /* UI */
    public void loadProfilePic(){
        loadProfilePic(getLoginType());
    }

    public void loadProfilePic(int loginType){
        profileCircleImageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (StringUtils.isNotEmpty(lastImageLoadedPath))
        {
            setProfilePicFromPath(lastImageLoadedPath);
            return;
        }

        switch (loginType)
        {
            case BDefines.BAccountType.Facebook:
                getProfileFromFacebook();
                break;

            case BDefines.BAccountType.Password:
            case BDefines.BAccountType.Custom:
            case BDefines.BAccountType.Register:
                if (profileUser==null)
                    setProfilePicFromURL(BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().metaStringForKey(BDefines.Keys.BPictureURL), false);
                else setProfilePicFromURL(profileUser.metaStringForKey(BDefines.Keys.BPictureURL), false);
                break;

            case BDefines.BAccountType.Anonymous:
                setInitialsProfilePic(BDefines.InitialsForAnonymous, true);

            case BDefines.BAccountType.Twitter:
                getProfileFromTwitter();
                break;
        }
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

    private PostProfilePic postProfilePic;

    public void setProfilePic(final Bitmap bitmap){
        // load image into imageview
        final int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();

        if (loadFromUrl != null)
            loadFromUrl.setKilled(true);

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

    public class LoadFromUrl implements ImageLoader.ImageListener{
        private boolean killed = false;

        private boolean saveAfterLoad = false;

        public LoadFromUrl(){

        }

        public LoadFromUrl(boolean saveAfterLoad){
            this.saveAfterLoad = saveAfterLoad;
        }

        public void setKilled(boolean killed) {
            this.killed = killed;
        }

        @Override
        public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {

            if (killed)
                return;

            if (response.getBitmap() != null) {
                setProfilePic(response.getBitmap());

                if (saveAfterLoad)
                    createTempFileAndSave(response.getBitmap());
            }
        }


        @Override
        public void onErrorResponse(VolleyError error) {
            if (DEBUG) Timber.e("Image Load Error: %s", error.getMessage());
            setInitialsProfilePic(false);
        }
    };

    private LoadFromUrl loadFromUrl;

    public void setProfilePicFromURL(String url, boolean saveAfterLoad){

        if (DEBUG) Timber.v("setProfilePicFromURL, Url: %s", url);

        // Set default.
        if (StringUtils.isEmpty(url))
        {
            // Loading the user image from robohash.
            String name = profileUser == null ? BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getMetaName() : profileUser.getMetaName();
            url = BDefines.getDefaultImageUrl("http://robohash.org/" + name,
                    BDefines.ImageProperties.INITIALS_IMAGE_SIZE, 
                    BDefines.ImageProperties.INITIALS_IMAGE_SIZE);
            
            saveAfterLoad = true;
        }

        if (loadFromUrl != null)
            loadFromUrl.setKilled(true);

        loadFromUrl = new LoadFromUrl(saveAfterLoad);

        VolleyUtils.getImageLoader().get(url, loadFromUrl);
    }

    /** Only for current user.*/
    public  Promise<String[], BError, SaveImageProgress> saveProfilePicToServer(String path, boolean setAsPic) {

        //  Loading the bitmap
        if (setAsPic)
        {
            setProfilePicFromPath(path);
        }

        return saveProfilePicToServer(path);
    }

    /** Only for current user.*/
    public void setProfilePicFromPath(String path){
        Bitmap b = ImageUtils.loadBitmapFromFile(path);

        if (b == null)
        {
            b = ImageUtils.loadBitmapFromFile(activity.getCacheDir().getPath() + path);
            if (b == null)
            {
                uiHelper.showAlertToast(R.string.unable_to_save_file);
                if (DEBUG) Timber.e("Cant save image to backendless file path is invalid: " + activity.getCacheDir().getPath() + path);
                return;
            }
        }

        setProfilePic(b);
    }

    /** Only for current user.*/
    public Promise<String[], BError, SaveImageProgress> saveProfilePicToServer(String path){
        Bitmap image = ImageUtils.getCompressed(path);

        Bitmap thumbnail = ImageUtils.getCompressed(path,
                BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE,
                BDefines.ImageProperties.MAX_IMAGE_THUMBNAIL_SIZE);

        return BNetworkManager.sharedManager().getNetworkAdapter().uploadImage(
                image, thumbnail)
                .done(new DoneCallback<String[]>() {
                    @Override
                    public void onDone(String[] data) {
                        // Saving the image to backendless.
                        final BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel();

                        currentUser.setMetaPictureUrl(data[0]);
                        currentUser.setMetaPictureThumbnail(data[1]);

                        BNetworkManager.sharedManager().getNetworkAdapter().pushUser();
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        if (DEBUG)
                            Timber.e("Backendless Exception while saving profile pic, message: %s", error.message);
                    }
                });
    }

    public void getProfileFromFacebook(){
        // Use facebook profile picture only if has no other picture saved.
        String imageUrl;
        if (profileUser==null)
            imageUrl = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getMetaPictureUrl();
        else imageUrl = profileUser.getMetaPictureUrl();

        if (StringUtils.isNotEmpty(imageUrl))
            setProfilePicFromURL(imageUrl, false);
        else
        {
            // Load the profile picture from facebook.
            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    HttpURLConnection client = null;
                    try {
                        String facebookId;
                        String authId;
                        if (profileUser==null)
                        {
                            authId = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getEntityID();
                        }
                        else {
                            authId = profileUser.getEntityID().replace(BDefines.ProviderString.Facebook + ":", "");
                            
                            if (StringUtils.isEmpty(authId)) {
                                authId = profileUser.getEntityID();
                            }
                        }
                        
                        facebookId = authId.replace(BDefines.ProviderString.Facebook + ":", "");
                        
                        if (DEBUG) Timber.d("Facebook Id: %s", facebookId);

                        String facebookImageUrl = BFacebookManager.getPicUrl(facebookId);
                        HttpURLConnection con = (HttpURLConnection) (new URL(facebookImageUrl).openConnection());
                        con.setInstanceFollowRedirects(false);
                        con.connect();
                        int responseCode = con.getResponseCode();
                        System.out.println(responseCode);
                        String location = con.getHeaderField("Location");
                        System.out.println(location);
                        
                        return location;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    setProfilePicFromURL(s, true);
                }
            }.execute();
        }

    }

    public void getProfileFromTwitter(){
        // Use facebook profile picture only if has no other picture saved.
        String savedUrl;

        if (profileUser==null)
            savedUrl = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getMetaPictureUrl();
        else savedUrl = profileUser.getMetaPictureUrl();

        if (StringUtils.isNotEmpty(savedUrl))
            setProfilePicFromURL(savedUrl, false);
        else {
            String imageUrl = TwitterManager.profileImageUrl;

            if (DEBUG) Timber.d("Twitter profile pic url: %s", TwitterManager.profileImageUrl);

            if (StringUtils.isNotEmpty(imageUrl))
            {
                // The default image suppied by twitter is 48px on 48px image so we want a bigget one.
                imageUrl = imageUrl.replace("_normal", "");
                setProfilePicFromURL(imageUrl, true);
            }
            else
            {
                if (DEBUG) Timber.d("cant get twitter profile picture.");
                setInitialsProfilePic(false);
            }
        }
    }

    public void setInitialsProfilePic(boolean save){
        String initials = "";

        String name;
        if (profileUser==null)
            name = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel().getMetaName();
        else{
            // We dont save initials image for other users.
            save = false;
            name = profileUser.getMetaName();
        }

        if (StringUtils.isEmpty(name))
            initials = BDefines.InitialsForAnonymous;
        else
        {
            String[] splited = name.split("\\s+");
            if (splited.length == 1)
                initials = String.valueOf(name.toUpperCase().charAt(0));
            else if (splited.length >= 2)
                initials = String.valueOf(splited[0].toUpperCase().charAt(0)) + String.valueOf(splited[1].toUpperCase().charAt(0));
            else initials = BDefines.InitialsForAnonymous;
        }

        setInitialsProfilePic(initials, save);
    }

    public void setInitialsProfilePic(final String initials, boolean save) {
        Bitmap bitmap = ImageUtils.getInitialsBitmap(Color.GRAY, Color.BLACK, initials);
        setProfilePic(bitmap);

        if (save)
            createTempFileAndSave(bitmap);
    }

    public boolean createTempFileAndSave(Bitmap bitmap){
        // Saving the image to tmp file.
        try {
            File tmp = File.createTempFile("Pic", ".jpg", activity.getCacheDir());
            ImageUtils.saveBitmapToFile(tmp, bitmap);
            saveProfilePicToServer(tmp.getPath(), false);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Bitmap scaleImage(Bitmap bitmap, int boundBoxInDp){
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

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);

        return bitmap;
    }

    public int handleResult(int requestCode, int resultCode, Intent data){
        if (data == null)
        {
            return NOT_HANDLED;
        }


        if (requestCode == PROFILE_PIC)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                Uri uri = data.getData();

                Uri outputUri = Uri.fromFile(new File(this.activity.getCacheDir(), "cropped.jpg"));
                crop = new Cropper(uri);

                Intent cropIntent = crop.getIntent(this.activity, outputUri);
                int request =Crop.REQUEST_CROP + PROFILE_PIC;

                if (fragment==null)
                    activity.startActivityForResult(cropIntent, request);
                else activity.startActivityFromFragment(fragment, cropIntent, request);

                return HANDELD;
            }
        }
        else  if (requestCode == Crop.REQUEST_CROP + PROFILE_PIC) {
            if (resultCode == Crop.RESULT_ERROR)
            {
                return ERROR;
            }

            try
            {
                File image;
                Uri uri = Crop.getOutput(data);

                if (DEBUG) Timber.d("Fetch image URI: %s", uri.toString());
                image = new File(this.activity.getCacheDir(), "cropped.jpg");

                lastImageLoadedPath = image.getPath();

                if (saveImageWhenLoaded)
                {
                    saveProfilePicToServer(lastImageLoadedPath, true)
                    .done(new DoneCallback<String[]>() {
                        @Override
                        public void onDone(String[] strings) {
                            // Resetting the selected path when done saving the image
                            lastImageLoadedPath = "";
                        }
                    });
                }
                else
                    setProfilePicFromPath(lastImageLoadedPath);

                return HANDELD;
            }
            catch (NullPointerException e){
                if (DEBUG) Timber.e("Null pointer when getting file.");
                uiHelper.showAlertToast(R.string.unable_to_fetch_image);
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


    
    
    
    
    
    
    public static View.OnClickListener getProfilePicClickListener(final Activity activity){
        return ChatSDKIntentClickListener.getPickImageClickListener(activity, PROFILE_PIC);
    }

    public static View.OnClickListener getProfilePicClickListener(final Activity activity, final Fragment fragment){
        return ChatSDKIntentClickListener.getPickImageClickListener(activity, fragment, PROFILE_PIC);
    }

    public static View.OnClickListener getPickImageClickListener(final FragmentActivity activity,final Fragment fragment, final int requestCode){
        return ChatSDKIntentClickListener.getPickImageClickListener(activity, fragment, requestCode);
    }

    public static View.OnClickListener getPickImageClickListener(final FragmentActivity activity,final DialogFragment fragment, final int requestCode){
        return ChatSDKIntentClickListener.getPickImageClickListener(activity, fragment, requestCode);
    }

    public Integer getLoginType(){
        return (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);
    }

    /** If set the helper will use this fragment when calling startActivityForResult*/
    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getLastImageLoadedPath() {
        return lastImageLoadedPath;
    }

    public void saveImageWhenLoaded(boolean saveImageWhenLoaded) {
        this.saveImageWhenLoaded = saveImageWhenLoaded;
    }

    public CircleImageView getProfilePic() {
        return profileCircleImageView;
    }

    public void setProfileUser(BUser profileUser) {
        this.profileUser = profileUser;
    }
}
