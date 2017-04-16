/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.abstracted;

import android.content.Intent;
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

import wanderingdevelopment.tk.sdkbaseui.R;
import com.braunster.chatsdk.Utils.Debug;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.ChatSDKProfileHelper;
import wanderingdevelopment.tk.sdkbaseui.FragmentTemplates.ChatSDKBaseFragment;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.Cropper;

import wanderingdevelopment.tk.sdkbaseui.view.CircleImageView;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class ChatSDKAbstractProfileFragment extends ChatSDKBaseFragment {

    protected static final int PHOTO_PICKER_ID = 100;

    private static final String TAG = ChatSDKAbstractProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;

    private static final String LOGIN_TYPE = "login_type";

    protected ChatSDKProfileHelper chatSDKProfileHelper;

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

        chatSDKProfileHelper = new ChatSDKProfileHelper((AppCompatActivity) getActivity(), profileCircleImageView, progressBar, chatSDKUiHelper, mainView);
        chatSDKProfileHelper.setFragment(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Long click will open the gallery so the user can change is picture.
        if (clickableProfilePic)
            profileCircleImageView.setOnClickListener(ChatSDKProfileHelper.getProfilePicClickListener((AppCompatActivity) getActivity(), this));

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
        return (Integer) BNetworkManager.getAuthInterface().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        chatSDKProfileHelper.handleResult(requestCode, resultCode, data);
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

    public void setProfilePicClickable(boolean clickableProfilePic) {
        this.clickableProfilePic = clickableProfilePic;
    }

    public abstract void logout();

    public void enableActionBarItems(boolean enableActionBarItems) {
        this.enableActionBarItems = enableActionBarItems;
    }


}
