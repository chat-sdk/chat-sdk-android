/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.events.PredicateFactory;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.chat.ChatActivity;
import io.reactivex.functions.Consumer;
import wanderingdevelopment.tk.sdkbaseui.Fragments.BaseFragment;
import wanderingdevelopment.tk.sdkbaseui.R;
import co.chatsdk.core.defines.Debug;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by itzik on 6/17/2014.
 */
public class PrivateThreadsFragment extends BaseFragment {

    private static final String TAG = PrivateThreadsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ConversationsFragment;

    protected ListView listThreads;
    protected AbstractThreadsListAdapter adapter;
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

        getActivity().registerReceiver(receiver, new IntentFilter(ChatActivity.ACTION_CHAT_CLOSED));

        NM.events().sourceOnMain()
                .filter(PredicateFactory.privateThreadsUpdated())
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(NetworkEvent networkEvent) throws Exception {
                loadData();
            }
        });

    }

    @Override
    public void initViews() {
        listThreads = (ListView) mainView.findViewById(R.id.list_threads);
        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progress_bar);
        initList();
    }

    private void initList() {

        // Create the adapter only if null, This is here so we wont
        // override the adapter given from the extended class with setAdapter.
        if (adapter == null)
            adapter = new ThreadsListAdapter((AppCompatActivity) getActivity());

        listThreads.setAdapter(adapter);
        listThreads.setClickable(true);

        if (onItemClickListener==null)
        {
            onItemClickListener = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startChatActivityForID(adapter.getItem(position).getId());
                }
            };
        }

        listThreads.setOnItemClickListener(onItemClickListener);

        if (onItemLongClickListener== null)
        {
            onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    showToastDialog("", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                            getResources().getString(R.string.cancel), null, new DeleteThread(adapter.getItem(position).getThread()));

                    return true;
                }
            };
        }

        listThreads.setOnItemLongClickListener(onItemLongClickListener);
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        adapter.setThreadItems(NM.thread().getThreads(ThreadType.Private));
    }

    @Override
    public void refreshForEntity(Object entity) {
        super.refreshForEntity(entity);
        if (adapter.getCount() == 0)
            return;

        adapter.replaceOrAddItem((BThread) entity);
        if (progressBar.getVisibility() == View.VISIBLE)
        {
            progressBar.setVisibility(View.GONE);
            listThreads.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void clearData() {
        if (adapter != null)
        {

            adapter.getThreadItems().clear();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!inflateMenuItems)
            return;

        super.onCreateOptionsMenu(menu, inflater);
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

        if (id == R.id.action_chat_sdk_add)
        {
            startPickFriendsActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
        }
    }


    public void setAdapter(AbstractThreadsListAdapter adapter) {
        this.adapter = adapter;
    }

    public void setInflateMenuItems(boolean inflateMenuItems) {
        this.inflateMenuItems = inflateMenuItems;
    }

    public void filterThreads(String text){
        adapter.filterItems(text);
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ChatActivity.ACTION_CHAT_CLOSED))
            {
                loadData();
            }
        }
    };

    public AbstractThreadsListAdapter getAdapter() {
        return adapter;
    }

}
