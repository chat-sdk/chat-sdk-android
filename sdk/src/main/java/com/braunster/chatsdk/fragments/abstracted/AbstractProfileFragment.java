package com.braunster.chatsdk.fragments.abstracted;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.volley.VolleyUtils;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.fragments.ChatSDKBaseFragment;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Cropper;
import com.braunster.chatsdk.parse.ParseUtils;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class AbstractProfileFragment extends ChatSDKBaseFragment {

    protected static final int PHOTO_PICKER_ID = 100;

    private static final String TAG = AbstractProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;

    private static final String LOGIN_TYPE = "login_type";

    protected Cropper crop;

    protected Bundle savedState;
    protected CircleImageView profileCircleImageView;
    protected ProgressBar progressBar;
    protected Integer loginType;
    private boolean enableActionBarItems = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        savedState = savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (DEBUG) Log.d(TAG, "onCreateView");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initToast();

        loginType = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);

        return mainView;
    }

    @Override
    public void initViews(){
        super.initViews();
        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);
        profileCircleImageView = (CircleImageView) mainView.findViewById(R.id.chat_sdk_circle_ing_profile_pic);
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        //endregion

        // Long click will open the gallery so the user can change is picture.
        profileCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent();
//                Crop.pickzImage(getActivity());
            }

            private void pickIntent(){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent,
                        "Complete action using"), PHOTO_PICKER_ID);
            }
        });

    }

    @Override
    public void clearData() {
        super.clearData();

        if (mainView != null)
        {
            profileCircleImageView.setImageResource(R.drawable.ic_action_user);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        //http://stackoverflow.com/a/15314508/2568492
        if (mainView == null)
        {
            if (savedState == null)
                return;

            outState.putInt(LOGIN_TYPE, savedState.getInt(LOGIN_TYPE));

            savedState.remove(LOGIN_TYPE);

            if (savedState.isEmpty())
                savedState = null;

            return;
        }

        outState.putInt(LOGIN_TYPE, loginType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
        {
            if (DEBUG) Log.e(TAG, "onActivityResult, Intent is null");
            return;
        }

        if (DEBUG) Log.v(TAG, "onActivityResult");

        if (requestCode == PHOTO_PICKER_ID)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (DEBUG) Log.d(TAG, "Result OK");
                Uri uri = data.getData();

                Uri outputUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped.jpg"));
                crop = new Cropper(uri);
                startActivityForResult(crop.getIntent(getActivity(), outputUri), Crop.REQUEST_CROP);

            }
        }
        else  if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == Crop.RESULT_ERROR)
            {
                if (DEBUG) Log.e(TAG, "Result Error");
                return;
            }

            try
            {
                File image;
                Uri uri = Crop.getOutput(data);

                if (DEBUG) Log.d(TAG, "Fetch image URI: " + uri.toString());
                image = new File(getActivity().getCacheDir(), "cropped.jpg");
                saveProfilePicToParse(image.getPath(), true);
            }
            catch (NullPointerException e){
                if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                showToast("Unable to fetch image");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!enableActionBarItems)
            return;

        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_logout, 12, "Logout");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icon_light_exit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_logout)
        {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*############################################*/
    /* UI*/
    protected void loadProfilePic(int loginType){
        profileCircleImageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        switch (loginType)
        {
            case BDefines.BAccountType.Facebook:
                getProfileFromFacebook();
                break;

            case BDefines.BAccountType.Password:
                setProfilePicFromURL(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().metaStringForKey(BDefines.Keys.BPictureURL));
                break;

            case BDefines.BAccountType.Anonymous:
                setInitialsProfilePic(BDefines.InitialsForAnonymous);

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

    protected void setProfilePic(final Bitmap bitmap){
        if (DEBUG) Log.v(TAG, "setProfilePic, Width: " + bitmap.getWidth() + ", Height: " + bitmap.getHeight());
        // load image into imageview
        final int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();

        if (loadFromUrl != null)
            loadFromUrl.setKilled(true);

        if (postProfilePic != null)
            profileCircleImageView.removeCallbacks(postProfilePic);

        // If the size of the container is 0 we will wait for the view to do onLayout and only then measure it.
        if (size == 0)
        {
            if (DEBUG) Log.d(TAG, "setProfilePic, Size == 0");
            postProfilePic = new PostProfilePic(bitmap);

            profileCircleImageView.post(postProfilePic);
        } else
        {
            if (DEBUG) Log.d(TAG, "setProfilePic, Has Size");
            profileCircleImageView.setImageBitmap(scaleImage(bitmap, size));
            progressBar.setVisibility(View.GONE);
            profileCircleImageView.setVisibility(View.VISIBLE);
        }
    }

    private class LoadFromUrl implements ImageLoader.ImageListener{
        private boolean killed = false;

        public void setKilled(boolean killed) {
            this.killed = killed;
        }

        @Override
        public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {

            if (killed)
                return;

            if (response.getBitmap() != null) {
                if (DEBUG) Log.v(TAG, "onResponse, Profile pic loaded from url.");
                setProfilePic(response.getBitmap());
            }
        }


        @Override
        public void onErrorResponse(VolleyError error) {
            if (DEBUG) Log.e(TAG, "Image Load Error: " + error.getMessage());
            setInitialsProfilePic();
        }
    };
    private LoadFromUrl loadFromUrl;

    protected void setProfilePicFromURL(String url){
        // Set default.
        if (url == null)
        {
            setInitialsProfilePic();
            return;
        }

        if (loadFromUrl != null)
            loadFromUrl.setKilled(true);

        loadFromUrl = new LoadFromUrl();

        VolleyUtils.getImageLoader().get(url, loadFromUrl);
    }

    protected void saveProfilePicToParse(String path, boolean setAsPic) {
        if (DEBUG) Log.v(TAG, "saveProfilePicToParse, Path: " + path);

        //  Loading the bitmap
        if (setAsPic)
        {
            if (DEBUG) Log.d(TAG, "SetAsPic");
            Bitmap b = ImageUtils.loadBitmapFromFile(path);

            if (b == null)
            {
                b = ImageUtils.loadBitmapFromFile(getActivity().getCacheDir().getPath() + path);
                if (b == null)
                {
                    showToast("Unable to save file...");
                    if (DEBUG) Log.e(TAG, "Cant save image to parse file path is invalid: " + getActivity().getCacheDir().getPath() + path);
                    return;
                }
            }
            setProfilePic(b);
        }

        saveProfilePicToParse(path);
    }

    protected void saveProfilePicToParse(String path){
        ParseUtils.saveImageFileToParseWithThumbnail(path, BDefines.ImageProperties.PROFILE_PIC_THUMBNAIL_SIZE, new ParseUtils.MultiSaveCompletedListener() {
            @Override
            public void onSaved(ParseException exception, String... data) {
                if (exception != null)
                {
                    if (DEBUG) Log.e(TAG, "Parse Exception while saving profile pic --- " + exception.getMessage());
                    return;
                }

                // Saving the image to parse.
                final BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

                currentUser.setMetadataString(BDefines.Keys.BPictureURL, data[0]);
                currentUser.setMetadataString(BDefines.Keys.BPictureURLThumbnail, data[1]);

                currentUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPictureURL, BMetadata.Type.STRING);
                currentUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPictureURLThumbnail, BMetadata.Type.STRING);

                BNetworkManager.sharedManager().getNetworkAdapter().pushUserWithCallback(null);
            }
        });
    }

    protected void getProfileFromFacebook(){
        // Use facebook profile picture only if has no other picture saved.
        String imageUrl = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPictureUrl();

        if (imageUrl != null)
            setProfilePicFromURL(imageUrl);
        else
        {
            // If there isnt any picture url saved we will save the fb profile pic to parse and save the url to the user.
            BFacebookManager.getUserDetails(new CompletionListenerWithData<GraphUser>() {
                @Override
                public void onDone(GraphUser graphUser) {
                    Log.d(TAG, "Name: " + graphUser.getName());

                    VolleyUtils.getImageLoader().get(BFacebookManager.getPicUrl(graphUser.getId()),
                            new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                                    if (response.getBitmap() != null) {
                                        setProfilePic(response.getBitmap());

                                        createTempFileAndSave(response.getBitmap());
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    setInitialsProfilePic();
                                    showToast("Unable to load user profile pic.");
                                }
                            });
                };

                @Override
                public void onDoneWithError(BError error) {
                    setInitialsProfilePic();
                    showToast("Unable to fetch user details from fb" + (error != null ? error.code == BError.Code.EXCEPTION ? error.message : "" : ""));
                }
            });
        }
    }

    protected void getProfileFromTwitter(){
        // Use facebook profile picture only if has no other picture saved.
        String savedUrl = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPictureUrl();

        if (StringUtils.isNotEmpty(savedUrl))
            setProfilePicFromURL(savedUrl);
        else {
            String imageUrl = TwitterManager.profileImageUrl;
            if (DEBUG) Log.d(TAG, "Image URL: " + imageUrl);
            if (StringUtils.isNotEmpty(imageUrl))
            {
                // The default image suppied by twitter is 48px on 48px image so we want a bigget one.
                imageUrl = imageUrl.replace("_normal", "");
                VolleyUtils.getImageLoader().get(imageUrl,
                        new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                                if (response.getBitmap() != null) {

                                    setProfilePic(response.getBitmap());

                                    createTempFileAndSave(response.getBitmap());
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                setInitialsProfilePic();
                                showToast("Unable to load user profile pic.");
                            }
                        }
                );
            }
        }
    }

    protected void setInitialsProfilePic(){

        String initials = "";

        String name = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaName();
        if (DEBUG) Log.v(TAG, "setInitialsProfilePic, Name: " + name);

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

        setInitialsProfilePic(initials);
    }

    protected void setInitialsProfilePic(final String initials) {
        if (DEBUG) Log.v(TAG, "setInitialsProfilePic, Initials: " + initials);
        Bitmap bitmap = ImageUtils.getInitialsBitmap(Color.GRAY, Color.BLACK, initials );
        setProfilePic(bitmap);
        createTempFileAndSave(bitmap);
    }

    protected boolean createTempFileAndSave(Bitmap bitmap){
        if (DEBUG) Log.v(TAG, "createTempFileAndSave");

        // Saving the image to tmp file.
        try {
            File tmp = File.createTempFile("Pic", ".jpg", getActivity().getCacheDir());
            ImageUtils.saveBitmapToFile(tmp, bitmap);
            if (DEBUG) Log.i(TAG, "Temp file path: " + tmp.getPath());
            saveProfilePicToParse(tmp.getPath(), false);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public abstract void logout();

    public void enableActionBarItems(boolean enableActionBarItems) {
        this.enableActionBarItems = enableActionBarItems;
    }

    /*############################################*/
    protected void indexUser(final BUser user, final String oldIndex, final String newIndex){
        BNetworkManager.sharedManager().getNetworkAdapter().removeUserFromIndex(user, oldIndex, new CompletionListener() {
            @Override
            public void onDone() {
                BNetworkManager.sharedManager().getNetworkAdapter().addUserToIndex(user, newIndex,null);
            }

            @Override
            public void onDoneWithError() {
                showToast("Cant set index.");
            }
        });
    }
}
