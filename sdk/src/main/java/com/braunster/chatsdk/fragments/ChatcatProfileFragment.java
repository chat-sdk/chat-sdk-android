/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractProfileFragment;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.countrypicker.Country;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatcatProfileFragment extends ChatSDKAbstractProfileFragment implements View.OnClickListener{


    private static final String TAG = ChatcatProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;

    private BUser profileUser;

    private boolean isCurrentUser = false;

    public static ChatcatProfileFragment newInstance() {
        ChatcatProfileFragment f = new ChatcatProfileFragment();
        f.setRetainInstance(true);
        f.setProfileUser(BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel());

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Timber.d("onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // If profile is the current user we allow editing the profile picture.
        clickableProfilePic  = profileUser.isMe();

        // Dont inflate the ChatSDKAbstractProfileFragment menu items.
        enableActionBarItems(false);

        initViews(inflater);

        loadData();

        return mainView;
    }

    public void initViews(LayoutInflater inflater){
        if (inflater != null)
            mainView = inflater.inflate(R.layout.chatcat_fragment_profile, null);
        else return;

        super.initViews();

        setupTouchUIToDismissKeyboard(mainView, R.id.chat_sdk_circle_ing_profile_pic);

        chatSDKProfileHelper.setProfileUser(profileUser);
    }

    @Override
    public void loadData() {
        super.loadData();

        setDetails((Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey));
    }

    @Override
    public void clearData() {
        super.clearData();

        if (mainView != null)
        {
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_name)).setText("");
        }
    }

    @Override
    public void logout() {
        // Logout and return to the login activity.
        BFacebookManager.logout(getActivity());

        BNetworkManager.sharedManager().getNetworkAdapter().logout();
        chatSDKUiHelper.startLoginActivity(true);
    }

    /** Fetching the user details from the user's metadata.*/
    private void setDetails(int loginType){
        if (mainView == null || getActivity() == null)
        {
            return;
        }

        BUser user = profileUser;

        String name = user.getName();

        if (StringUtils.isNotEmpty(name))
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_name)).setText(name);


        String gender = user.metaStringForKey(BDefines.Keys.BGender);

        String country = user.metaStringForKey(BDefines.Keys.BCountry);

        String status = user.metaStringForKey(BDefines.Keys.BDescription);

        String location = user.metaStringForKey(BDefines.Keys.BCity);

        String dateOfBirth = user.metaStringForKey(BDefines.Keys.BDateOfBirth);

        // Loading the user country icon, If not exist we will hide the icon.
        if (StringUtils.isNotEmpty(country)) {
            ((ImageView) mainView.findViewById(R.id.chat_sdk_country_ic)).setImageResource(Country.getResId(country));
            mainView.findViewById(R.id.chat_sdk_country_ic).setVisibility(View.VISIBLE);
        }
        else{
            hideViews(mainView.findViewById(R.id.chat_sdk_country_ic));
        }

        // Loading the user status, If not exist we will hide the status line and header.
        if (StringUtils.isNotEmpty(status)) {
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_status)).setText(status);

            mainView.findViewById(R.id.chat_sdk_txt_status).setVisibility(View.VISIBLE);
            mainView.findViewById(R.id.chat_sdk_txt_status_header).setVisibility(View.VISIBLE);
        }
        else {
            hideViews(mainView.findViewById(R.id.chat_sdk_txt_status), mainView.findViewById(R.id.chat_sdk_txt_status_header));
        }

        if (StringUtils.isNotEmpty(location)) {
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_location)).setText(location);
            mainView.findViewById(R.id.relative_location).setVisibility(View.VISIBLE);
        }
        else {
            hideViews(mainView.findViewById(R.id.relative_location));
        }

        if (StringUtils.isNotEmpty(dateOfBirth)){
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_age)).setText(String.format("%s years old", user.age()));
            mainView.findViewById(R.id.relative_age).setVisibility(View.VISIBLE);
        }
        else
            hideViews(mainView.findViewById(R.id.relative_age));


        if (StringUtils.isEmpty(gender)) {
            hideViews(mainView.findViewById(R.id.chat_sdk_ic_gender));
        }
        else {
            ImageView v = (ImageView) mainView.findViewById(R.id.chat_sdk_ic_gender);

            v.setVisibility(View.VISIBLE);

            v.setBackgroundResource( gender.equals(BDefines.Keys.BMale) ? R.drawable.ic_male : R.drawable.ic_female);
        }

        if (DEBUG) Timber.d("loading user details, Name: %s, Status: %s, Country: %s, Location: %s, Gender: %s", name, status, country, location, gender);

        // If the user is the current user we will try to get the image using the login type,
        // else we will just load it from url.
        if (profileUser.isMe())
        {
            chatSDKProfileHelper.loadProfilePic(getLoginType());
        }
        else
        {
            chatSDKProfileHelper.loadProfilePic();

            View addFriend = getAddFriendView();
            addFriend.setVisibility(View.VISIBLE);
            addFriend.setOnClickListener(this);

            View block = getBlockView();
            block.setVisibility(View.VISIBLE);
            block.setOnClickListener(this);


            setIsBlocked(getNetworkAdapter().blockedUsers().contains(profileUser), false);
            setIsFriend(getNetworkAdapter().friends().contains(profileUser), false);
        }
    }

    private void hideViews(View... views){
        for (View v : views)
            v.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (profileUser.isMe())
        {
            MenuItem item =
                    menu.add(Menu.NONE, R.id.action_chat_sdk_edit, 13, getString(R.string.action_edit));
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            item.setIcon(R.drawable.ic_edit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        if (item.getItemId() == R.id.action_chat_sdk_edit)
        {
            chatSDKUiHelper.startEditProfileActivity();

            getActivity().overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public void setProfileUser(BUser profileUser) {
        this.profileUser = profileUser;
    }

    @Override
    public void onClick(View v) {

        int i = v.getId();
        if (i == R.id.relative_add_friend) {
            setIsFriend(!getAddFriendView().isSelected(), true);
        }
        else if (i == R.id.relative_block) {
            setIsBlocked(!getBlockView().isSelected(), true);
        }
    }

    private void setIsBlocked(final boolean isBlock, final boolean setRemote){

        if (setRemote)
            showProgDialog(!isBlock ? R.string.unblocking_user_progress : R.string.blocking_user_progress);

        DoneCallback<Void> success = new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {

                getBlockView().setSelected(isBlock);

                ((TextView) getBlockView().findViewById(R.id.txt_block)).setText(isBlock ? R.string.unblock_user_button : R.string.block_user_button);


                if (setRemote)
                    dismissProgDialog();
            }
        };

        FailCallback<BError> fail = new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {

                if (setRemote)
                    dismissProgDialog();
            }
        };

        if (setRemote)
        {
            if (isBlock)
                getNetworkAdapter().blockUser(profileUser).then(success, fail);
            else
                getNetworkAdapter().unblockUser(profileUser).then(success, fail);
        }
        else
        {
            success.onDone(null);
        }
    }


    private void setIsFriend(final boolean isFriend, final boolean setRemote){

        if (setRemote)
            showProgDialog(!isFriend ? R.string.unfriending_user_progress : R.string.adding_friend_progress);

        DoneCallback<Void> success = new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {

                getAddFriendView().setSelected(isFriend);

                ((TextView) getAddFriendView().findViewById(R.id.txt_friend)).setText(isFriend ? R.string.unfriend_button : R.string.add_friend_button);

                if (setRemote)
                    dismissProgDialog();
            }
        };

        FailCallback<BError> fail = new FailCallback<BError>() {
            @Override
            public void onFail(BError bError) {

                if (setRemote)
                    dismissProgDialog();
            }
        };

        if (setRemote) {
            if (isFriend)
                getNetworkAdapter().addFriends(profileUser).then(success, fail);
            else
                getNetworkAdapter().removeFriend(profileUser).then(success, fail);
        }
        else
        {
            success.onDone(null);
        }
    }

    private View getBlockView(){
        return mainView.findViewById(R.id.relative_block);
    }

    private View getAddFriendView(){
        return mainView.findViewById(R.id.relative_add_friend);
    }
}
