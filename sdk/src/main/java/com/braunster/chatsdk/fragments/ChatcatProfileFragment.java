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
import com.countrypicker.Country;

import org.apache.commons.lang3.StringUtils;

import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatcatProfileFragment extends ChatSDKAbstractProfileFragment {


    private static final String TAG = ChatcatProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;
    
    public static ChatcatProfileFragment newInstance() {
        ChatcatProfileFragment f = new ChatcatProfileFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

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

        // Changing the weight of the view according to orientation.
        // This will make sure (hopefully) there is enough space to show the views in landscape mode.
//        int currentOrientation = getResources().getConfiguration().orientation;
//        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
//            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.frame_profile_image_container).getLayoutParams();
//            layoutParams.weight = 3;
//            mainView.findViewById(R.id.frame_profile_image_container).setLayoutParams(layoutParams);
//        }
//        else
//        {
//            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.frame_profile_image_container).getLayoutParams();
//            layoutParams.weight = 2;
//            mainView.findViewById(R.id.linear).setLayoutParams(layoutParams);
//        }

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

        BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel();

        String name = user.getMetaName();

        if (StringUtils.isNotEmpty(name))
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_name)).setText(name);

        String country = user.metaStringForKey(BDefines.Keys.BCountry);

        String status = user.metaStringForKey(BDefines.Keys.BStatus);

        String location = user.metaStringForKey(BDefines.Keys.BLocation);

        // Loading the user country icon, If not exist we will hide the icon.
        if (StringUtils.isNotEmpty(country))
        {
            ((ImageView) mainView.findViewById(R.id.chat_sdk_country_ic)).setImageResource(Country.getResId(country));
            mainView.findViewById(R.id.chat_sdk_country_ic).setVisibility(View.VISIBLE);
        }
        else mainView.findViewById(R.id.chat_sdk_country_ic).setVisibility(View.INVISIBLE);

        // Loading the user status, If not exist we will hide the status line and header.
        if (StringUtils.isNotEmpty(status))
        {
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_status)).setText(status);

            mainView.findViewById(R.id.chat_sdk_txt_status).setVisibility(View.VISIBLE);
            mainView.findViewById(R.id.chat_sdk_txt_status_header).setVisibility(View.VISIBLE);
        }
        else {
            mainView.findViewById(R.id.chat_sdk_txt_status).setVisibility(View.GONE);
            mainView.findViewById(R.id.chat_sdk_txt_status_header).setVisibility(View.GONE);
        }

        if (StringUtils.isNotEmpty(location))
        {
            ((TextView) mainView.findViewById(R.id.chat_sdk_txt_location)).setText(location);
            mainView.findViewById(R.id.relative_location).setVisibility(View.VISIBLE);
        }
        else
            mainView.findViewById(R.id.relative_location).setVisibility(View.INVISIBLE);

        if (DEBUG) Timber.d("loading user details, Name: %s, Status: %s, Country: %s, Location: %s", name, status, country, location);

        chatSDKProfileHelper.loadProfilePic(loginType);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_edit, 13, getString(R.string.action_edit));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_edit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        if (item.getItemId() == R.id.action_chat_sdk_edit)
        {
            chatSDKUiHelper.startEditProfileActivity(getNetworkAdapter().currentUserModel().getId());

            getActivity().overridePendingTransition(R.anim.slide_bottom_top, R.anim.dummy);
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
}
