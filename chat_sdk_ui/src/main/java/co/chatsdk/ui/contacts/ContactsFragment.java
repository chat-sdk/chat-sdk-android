/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.chatsdk.core.dao.BUser;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends AbstractContactsFragment {

    public static ContactsFragment newInstance() {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(MODE_LOAD_CONTACTS);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newInstance(int loadingMode, int clickMode, Object extraData) {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(loadingMode);
        f.setClickMode(clickMode);
        f.setExtraData(extraData);
        return f;
    }

    public static ContactsFragment newInstance(int loadingMode, int clickMode, List<BUser> sourceUsers, Object extraData) {
        ContactsFragment f = new ContactsFragment();
        f.setLoadingMode(loadingMode);
        f.setClickMode(clickMode);
        f.setExtraData(extraData);
        f.setSourceUsers(sourceUsers);
        return f;
    }

    public static ContactsFragment newDialogInstance(int mode, String extraData, String title) {
        ContactsFragment f = new ContactsFragment();
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
    public static ContactsFragment newThreadUsersDialogInstance(String threadID, String title, boolean withUpdates) {
        ContactsFragment f = new ContactsFragment();
        f.setTitle(title);

        f.setLoadingMode(MODE_LOAD_THREAD_USERS);
        f.setDialog();
        f.setExtraData(threadID);
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    public static ContactsFragment newDialogInstance(int loadingMode, int clickMode, String title, Object extraData) {
        ContactsFragment f = new ContactsFragment();
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

        loadData();

        return mainView;
    }

}
