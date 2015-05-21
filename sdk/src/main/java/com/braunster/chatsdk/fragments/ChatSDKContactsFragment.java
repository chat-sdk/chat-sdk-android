/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractContactsFragment;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKContactsFragment extends ChatSDKAbstractContactsFragment {

    private static final String TAG = ChatSDKContactsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ContactsFragment;

    /* Initializers.*/
    public static ChatSDKContactsFragment newInstance(String eventTAG) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setLoadingMode(MODE_LOAD_CONTACTS);
        f.setEventTAG(eventTAG);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ChatSDKContactsFragment newInstance(int loadingMode, int clickMode, Object extraData) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setLoadingMode(loadingMode);
        f.setClickMode(clickMode);
        f.setExtraData(extraData);
        return f;
    }

    public static ChatSDKContactsFragment newInstance(int loadingMode, int clickMode, List<BUser> sourceUsers, Object extraData) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setLoadingMode(loadingMode);
        f.setClickMode(clickMode);
        f.setExtraData(extraData);
        f.setSourceUsers(sourceUsers);
        return f;
    }

    public static ChatSDKContactsFragment newDialogInstance(int mode, String extraData, String title) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setDialog();
        f.setTitle(title);
        f.setExtraData(extraData);
        f.setLoadingMode(mode);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    /** Creates a new contact dialog.
     * @param threadID - The id of the thread that his users is the want you want to show.
     * @param title - The title of the dialog.
     * @param withUpdates - the dialog will listen to user details changes.*/
    public static ChatSDKContactsFragment newThreadUsersDialogInstance(String threadID, String title, boolean withUpdates) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setTitle(title);

        if (withUpdates)
            f.setEventTAG(DaoCore.generateEntity());

        f.setLoadingMode(MODE_LOAD_THREAD_USERS);
        f.setDialog();
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ChatSDKContactsFragment newDialogInstance(int loadingMode, int clickMode, String title, Object extraData) {
        ChatSDKContactsFragment f = new ChatSDKContactsFragment();
        f.setDialog();
        f.setLoadingMode(loadingMode);
        f.setExtraData(extraData);
        f.setClickMode(clickMode);
        f.setTitle(title);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mainView = inflater.inflate(R.layout.chat_sdk_fragment_contacts, null);

        initViews();

        loadDataOnBackground();

        return mainView;
    }

}
