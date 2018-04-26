/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.NM;
import co.chatsdk.ui.R;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.ui.main.BaseFragment;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by itzik on 6/17/2014.
 */
public class PrivateThreadsFragment extends BaseFragment {

    protected RecyclerView recyclerView;
    protected ThreadsListAdapter adapter;

    protected boolean inflateMenuItems = true;

    public static PrivateThreadsFragment newInstance() {
        PrivateThreadsFragment f = new PrivateThreadsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(inflateMenuItems);

        NM.events().sourceOnMain()
                .filter(NetworkEvent.filterPrivateThreadsUpdated())
                .subscribe(networkEvent -> {
                    if(tabIsVisible) {
                        reloadData();
                    }
                });

        NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .subscribe(networkEvent -> {
                    if(tabIsVisible) {
                        adapter.setTyping(networkEvent.thread, networkEvent.text);
                        adapter.notifyDataSetChanged();
                    }
                });

    }

    public void initViews() {
        recyclerView = mainView.findViewById(R.id.list_threads);
        initList();
    }

    protected void initList() {

        // Create the adapter only if null, This is here so we wont
        // override the adapter given from the extended class with setAdapter.
        adapter = new ThreadsListAdapter(getActivity());

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);

        adapter.onClickObservable().subscribe(thread -> InterfaceManager.shared().a.startChatActivityForID(getContext(), thread.getEntityID()));

        adapter.onLongClickObservable().subscribe(thread -> DialogUtils.showToastDialog(getContext(), "", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                getResources().getString(R.string.cancel), null, () -> {
                    NM.thread().deleteThread(thread)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new CompletableObserver() {
                                @Override
                                public void onSubscribe(Disposable d) {
                                }

                                @Override
                                public void onComplete() {
                                    adapter.clearData();
                                    reloadData();
                                    ToastHelper.show(getContext(), getString(R.string.delete_thread_success_toast));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    ToastHelper.show(getContext(), getString(R.string.delete_thread_fail_toast));
                                }
                            });
                    return null;
                }));
    }

    public void clearData() {
        if (adapter != null) {
            adapter.clearData();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!inflateMenuItems)
            return;

        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, getActivity().getString(R.string.add_conversation));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.chat_sdk_activity_threads, container, false);

        initViews();

        reloadData();

        return mainView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if (!inflateMenuItems)
            return super.onOptionsItemSelected(item);

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add) {
            InterfaceManager.shared().a.startSelectContactsActivity(getContext());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void reloadData() {
        if(adapter != null) {
            adapter.clearData();
            adapter.updateThreads(NM.thread().getThreads(ThreadType.Private));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void setTabVisibility (boolean isVisible) {
        super.setTabVisibility(isVisible);
        reloadData();
    }

}
