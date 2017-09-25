/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.concurrent.Callable;

import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NM;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.R;

/**
 * Created by itzik on 6/17/2014.
 */
public class PrivateThreadsFragment extends BaseFragment {

    protected ListView threadsListView;
    protected ThreadsListAdapter adapter;
    protected ProgressBar progressBar;

    protected boolean inflateMenuItems = true;

    protected AdapterView.OnItemLongClickListener onItemLongClickListener;
    protected AdapterView.OnItemClickListener onItemClickListener;

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
                .subscribe(new Consumer<NetworkEvent>() {
                    @Override
                    public void accept(NetworkEvent networkEvent) throws Exception {
                        loadData();
                    }
                });
    }

    public void initViews() {
        threadsListView = (ListView) mainView.findViewById(R.id.list_threads);
        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progress_bar);
        initList();
    }

    private void initList() {

        // Create the adapter only if null, This is here so we wont
        // override the adapter given from the extended class with setAdapter.
        if (adapter == null) {
            adapter = new ThreadsListAdapter((AppCompatActivity) getActivity());
        }

        threadsListView.setAdapter(adapter);
        threadsListView.setClickable(true);

        if (onItemClickListener == null) {
            onItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    InterfaceManager.shared().a.startChatActivityForID(adapter.getItem(position).getEntityID());
                }
            };
        }

        threadsListView.setOnItemClickListener(onItemClickListener);

        if (onItemLongClickListener == null) {
            onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    showToastDialog("", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                            getResources().getString(R.string.cancel), null, new Callable() {
                                @Override
                                public Object call() throws Exception {
                                    NM.thread().deleteThread(adapter.getItem(position).getThread())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new CompletableObserver() {
                                        @Override
                                        public void onSubscribe(Disposable d) {
                                        }

                                        @Override
                                        public void onComplete() {
                                            loadData();
                                            ToastHelper.show(getString(R.string.delete_thread_success_toast));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            ToastHelper.show(getString(R.string.delete_thread_fail_toast));
                                        }
                                    });
                                    return null;
                                }
                            });

                    return true;
                }
            };
        }

        threadsListView.setOnItemLongClickListener(onItemLongClickListener);
    }

    public void loadData() {
        adapter.setAllItems(NM.thread().getThreads(ThreadType.Private));
    }

    public void clearData() {
        if (adapter != null) {
            adapter.getAllItems().clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!inflateMenuItems)
            return;

        super.onCreateOptionsMenu(menu, inflater);
        // TODO: Localise
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add Conversation");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.chat_sdk_activity_threads, container, false);

        initViews();

        loadData();

        return mainView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if (!inflateMenuItems)
            return super.onOptionsItemSelected(item);

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add) {
            InterfaceManager.shared().a.startSelectContactsActivity();
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
    public void onDestroy() {
        super.onDestroy();
    }

}
