/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.fragments.abstracted;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.TimingLogger;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.activities.abstracted.ChatSDKAbstractChatActivity;
import com.braunster.chatsdk.adapter.ChatSDKThreadsListAdapter;
import com.braunster.chatsdk.adapter.abstracted.ChatSDKAbstractThreadsListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.fragments.ChatSDKBaseFragment;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.object.UIUpdater;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKAbstractConversationsFragment extends ChatSDKBaseFragment {

    private static final String TAG = ChatSDKAbstractConversationsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ConversationsFragment;
    public static final String APP_EVENT_TAG= "ConverstaionFragment";

    protected ListView listThreads;
    protected ChatSDKAbstractThreadsListAdapter adapter;
    protected ProgressBar progressBar;

    protected TimingLogger timings;
    protected UIUpdater uiUpdater;

    protected boolean inflateMenuItems = true;

    protected AdapterView.OnItemLongClickListener onItemLongClickListener;
    protected AdapterView.OnItemClickListener onItemClickListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().registerReceiver(receiver, new IntentFilter(ChatSDKAbstractChatActivity.ACTION_CHAT_CLOSED));
    }

    @Override
    public void initViews() {
        listThreads = (ListView) mainView.findViewById(R.id.list_threads);
        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progress_bar);
        initList();
    }

    private void initList(){

        // Create the adpater only if null, This is here so we wont override the adapter given from the extended class with setAdapter.
        if (adapter == null)
            adapter = new ChatSDKThreadsListAdapter(getActivity());

        listThreads.setAdapter(adapter);

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
                    showAlertDialog("", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                            getResources().getString(R.string.cancel), null, new DeleteThread(adapter.getItem(position).getEntityId()));

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

        adapter.setThreadItems(BNetworkManager.sharedManager().getNetworkAdapter().threadItemsWithType(BThread.Type.Private, adapter.getItemMaker()));
    }

    @Override
    public void loadDataOnBackground() {
        super.loadDataOnBackground();

        if (DEBUG) timings = new TimingLogger(TAG.substring(0, 21), "loadDataOnBackground");

        if (mainView == null)
        {
            return;
        }

        final boolean isFirst;
        if (uiUpdater != null)
        {
            isFirst = false;
            uiUpdater.setKilled(true);
            ChatSDKAbstractConversationsFragmentChatSDKThreadPool.getInstance().removeSchedule(uiUpdater);
        }
        else
        {
            isFirst = true;
        }

        final boolean hasItems = adapter != null && adapter.getThreadItems().size() > 0;

        if (isFirst && !hasItems) {
            loadData();
        }

        uiUpdater = new UIUpdater() {
            @Override
            public void run() {

                if (isKilled() && !isFirst && hasItems)
                {
                    return;

                }

                if (DEBUG) {
                    timings.addSplit("Loading threads");
                }

                List list = BNetworkManager.sharedManager().getNetworkAdapter().threadItemsWithType(BThread.Type.Private, adapter.getItemMaker());

                if (DEBUG) {
                    timings.addSplit("Loading threads");
                }

                uiUpdater = null;

                Message message = new Message();
                message.obj = list;
                message.what = 1;
                handler.sendMessageAtFrontOfQueue(message);

                if (DEBUG) timings.addSplit("Sending message to handler.");
            }
        };

        ChatSDKAbstractConversationsFragmentChatSDKThreadPool.getInstance().scheduleExecute(uiUpdater,isFirst ? 1 : 0);
    }

    @Override
    public void refreshForEntity(Entity entity) {
        super.refreshForEntity(entity);
        if (adapter.getCount() == 0)
            return;;

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
            if (uiUpdater != null)
                uiUpdater.setKilled(true);

            adapter.getThreadItems().clear();
            adapter.notifyDataSetChanged();
        }
    }

    private class UpdateHandler extends Handler{
        
        public UpdateHandler(Looper mainLooper) {
            super(mainLooper);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:
                    adapter.setThreadItems((List<ChatSDKThreadsListAdapter.ThreadListItem>) msg.obj);
                    if (progressBar.getVisibility() == View.VISIBLE)
                    {
                        progressBar.setVisibility(View.INVISIBLE);
                        listThreads.setVisibility(View.VISIBLE);
                    }
                    if (DEBUG) {
                        timings.addSplit("Updating UI");
                        timings.dumpToLog();
                        timings.reset(TAG.substring(0, 21), "loadDataOnBackground");
                    }
                    break;
            }
        }

    }
    
    private UpdateHandler handler = new UpdateHandler(Looper.getMainLooper());

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

//        loadDataOnBackground();

        BatchedEvent batchedEvents = new BatchedEvent(APP_EVENT_TAG, "", Event.Type.AppEvent, handler);
        batchedEvents.setBatchedAction(Event.Type.AppEvent, 3000, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                loadDataOnBackground();
            }
        });

        getNetworkAdapter().getEventManager().removeEventByTag(APP_EVENT_TAG);
        getNetworkAdapter().getEventManager().addEvent(batchedEvents);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
        }
    }


    public void setAdapter(ChatSDKAbstractThreadsListAdapter adapter) {
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
            if (intent.getAction().equals(ChatSDKAbstractChatActivity.ACTION_CHAT_CLOSED))
            {
                loadDataOnBackground();
            }
        }
    };

    public ChatSDKAbstractThreadsListAdapter getAdapter() {
        return adapter;
    }

    /** FIXME not sure if needed.
     * Created by braunster on 18/08/14.
     */
    private static class ChatSDKAbstractConversationsFragmentChatSDKThreadPool {
        // Sets the amount of time an idle thread waits before terminating
        private static final int KEEP_ALIVE_TIME = 3;
        // Sets the Time Unit to seconds
        private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        private LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        /*
         * Gets the number of available cores
         * (not always the same as the maximum number of cores)
         */
        private static int NUMBER_OF_CORES =
                Runtime.getRuntime().availableProcessors();

        private ThreadPoolExecutor threadPool;
        private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

        private static ChatSDKAbstractConversationsFragmentChatSDKThreadPool instance;

        public static ChatSDKAbstractConversationsFragmentChatSDKThreadPool getInstance() {
            if (instance == null)
                instance = new ChatSDKAbstractConversationsFragmentChatSDKThreadPool();
            return instance;
        }

        private ChatSDKAbstractConversationsFragmentChatSDKThreadPool(){
            
            if (NUMBER_OF_CORES <= 0)
                NUMBER_OF_CORES = 2;
            
            // Creates a thread pool manager
            threadPool = new ThreadPoolExecutor(
                    NUMBER_OF_CORES,       // Initial pool size
                    NUMBER_OF_CORES,       // Max pool size
                    KEEP_ALIVE_TIME,
                    KEEP_ALIVE_TIME_UNIT,
                    workQueue);

            scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(NUMBER_OF_CORES);

        }

        public void execute(Runnable runnable){
            threadPool.execute(runnable);
        }

        public void scheduleExecute(Runnable runnable, long delay){
            scheduledThreadPoolExecutor.schedule(runnable, delay, TimeUnit.SECONDS);
        }

        public boolean removeSchedule(Runnable runnable){
            return scheduledThreadPoolExecutor.remove(runnable);
        }
    }
}
