/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.profile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.soundcloud.android.crop.Crop;

import java.io.File;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageUploadResult;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.R;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.helpers.ProfilePictureChooserOnClickListener;
import de.hdodenhof.circleimageview.CircleImageView;

import co.chatsdk.ui.utils.Cropper;
import id.zelory.compressor.Compressor;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

import static co.chatsdk.ui.helpers.ProfilePictureChooserOnClickListener.PROFILE_PIC;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class AbstractProfileFragment extends BaseFragment {

    public static final int ERROR = 1991, NOT_HANDLED = 1992, HANDLED = 1993;

    protected Cropper crop;

    protected CircleImageView profileCircleImageView;
    protected ProgressBar progressBar;
    private boolean enableActionBarItems = true;

    protected boolean clickableProfilePic = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(enableActionBarItems);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

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
        super.onResume();

        // Long click will open the gallery so the user can change is picture.
        if (clickableProfilePic) {
            profileCircleImageView.setOnClickListener(new ProfilePictureChooserOnClickListener((AppCompatActivity) getActivity(), this));
        }

    }

    @Override
    public void clearData() {
        super.clearData();

        if (mainView != null)
        {
            profileCircleImageView.setImageBitmap(null);
            profileCircleImageView.setImageResource(R.drawable.ic_action_user);
        }
    }

    protected Integer getLoginType(){
        return (Integer) NM.auth().getLoginInfo().get(Defines.Prefs.AccountTypeKey);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PROFILE_PIC)
        {
            if (resultCode == AppCompatActivity.RESULT_OK)
            {
                Uri uri = data.getData();

                Uri outputUri = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped.jpg"));
                crop = new Cropper(uri);

                Intent cropIntent = crop.getIntent(getActivity(), outputUri);
                int request = Crop.REQUEST_CROP + PROFILE_PIC;

                getActivity().startActivityFromFragment(this, cropIntent, request);
            }
        }
        else  if (requestCode == Crop.REQUEST_CROP + PROFILE_PIC) {
            try
            {
                File image = new File(getActivity().getCacheDir(), "cropped.jpg");
                saveProfilePicToServer(image.getPath()).subscribe();
            }
            catch (NullPointerException e){
                UIHelper.getInstance().showToast(R.string.unable_to_fetch_image);
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
        item.setIcon(R.drawable.ic_cancel);
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

    public abstract void logout();

    public void enableActionBarItems(boolean enableActionBarItems) {
        this.enableActionBarItems = enableActionBarItems;
    }

    private Completable saveProfilePicToServer(final String path){
        return Single.create(new SingleOnSubscribe<File>() {
            @Override
            public void subscribe(SingleEmitter<File> e) throws Exception {
                File image = new Compressor(getActivity())
                        .setMaxHeight(Defines.ImageProperties.MAX_HEIGHT_IN_PX)
                        .setMaxWidth(Defines.ImageProperties.MAX_WIDTH_IN_PX)
                        .compressToFile(new File(path));
                e.onSuccess(image);
            }
        }).flatMapCompletable(new Function<File, CompletableSource>() {
            @Override
            public CompletableSource apply(@NonNull File file) throws Exception {

                // Saving the image to backendless.
                final BUser currentUser = NM.currentUser();

                // TODO: Are we handling the error here
                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                if(NM.upload() != null) {
                    return NM.upload().uploadImage(bitmap).flatMapCompletable(new Function<MessageUploadResult, Completable>() {
                        @Override
                        public Completable apply(MessageUploadResult profileImageUploadResult) throws Exception {


                            currentUser.setAvatarURL(profileImageUploadResult.imageURL);
                            currentUser.setThumbnailURL(profileImageUploadResult.thumbnailURL);

                            return Completable.complete();
                        }
                    });
                }
                else {

                    // Move the image to the standard profile URL
                    String path = ImageUtils.saveToInternalStorage(bitmap, currentUser.getEntityID());

                    currentUser.setAvatarURL(path);
                    // Reset the hash code to force the image to be uploaded
                    currentUser.setAvatarHash("");
                    return Completable.complete();
                }
            }
        }).concatWith(NM.core().pushUser());
    }

}
