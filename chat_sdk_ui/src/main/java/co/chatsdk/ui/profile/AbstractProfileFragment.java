/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.profile;

import android.content.Intent;
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

import co.chatsdk.core.types.Defines;
import co.chatsdk.ui.R;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.helpers.ProfilePictureChooserOnClickListener;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.UserAvatarHelper;
import de.hdodenhof.circleimageview.CircleImageView;

import co.chatsdk.ui.utils.Cropper;

import static co.chatsdk.ui.helpers.ProfilePictureChooserOnClickListener.PROFILE_PIC;

/**
 * Created by itzik on 6/17/2014.
 */
@Deprecated
public abstract class AbstractProfileFragment extends BaseFragment {

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

    public void initViews(){

        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);
        profileCircleImageView = (CircleImageView) mainView.findViewById(R.id.ivAvatar);
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
                UserAvatarHelper.saveProfilePicToServer(image.getPath(), this.getActivity());
            }
            catch (NullPointerException e){
                UIHelper.shared().showToast(R.string.unable_to_fetch_image);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!enableActionBarItems)
            return;

        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_settings, 12, getString(R.string.action_settings));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icn_24_settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_settings)
        {
            showSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public abstract void showSettings();

    public void enableActionBarItems(boolean enableActionBarItems) {
        this.enableActionBarItems = enableActionBarItems;
    }

}
