/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.view.MenuItem;

import java.util.List;
import java.util.concurrent.Callable;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.CompletableObserver;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by itzik on 6/17/2014.
 */
public class PrivateThreadsFragment extends ThreadsFragment {

    @Override
    public void initViews() {
        super.initViews();

        disposableList.add(adapter.onLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), "", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                getResources().getString(R.string.cancel), null, () -> {
                    disposableList.add(ChatSDK.thread().deleteThread(thread)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                adapter.clearData();
                                reloadData();
                            }, throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
                    return null;
                })));
    }

    @Override
    protected Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterPrivateThreadsUpdated();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_add) {
            ChatSDK.ui().startCreateThreadActivity(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected List<Thread> getThreads() {
        return ChatSDK.thread().getThreads(ThreadType.Private);
    }

}
