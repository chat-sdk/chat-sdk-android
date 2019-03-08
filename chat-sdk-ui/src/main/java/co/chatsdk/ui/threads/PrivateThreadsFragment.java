/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.view.MenuItem;

import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;

/**
 * Created by itzik on 6/17/2014.
 */
public class PrivateThreadsFragment extends ThreadsFragment {

    @Override
    public void initViews() {
        super.initViews();

        addLongClickListenerToDeleteThread(false);
    }

    @Override
    protected Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterPrivateThreadsUpdated();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add) {
            ChatSDK.ui().startSelectContactsActivity(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<Thread> getThreads() {
        return ChatSDK.thread().getThreads(ThreadType.Private);
    }

}
