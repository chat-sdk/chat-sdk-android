package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.network.firebase.EventManager;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.UIUpdater;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ThreadsFragment extends BaseFragment {

    //TODO add selection of thread type to see.

    private static final String TAG = ThreadsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ThreadsFragment;
    public static final String APP_EVENT_TAG= "ChatRoomsFrag";

    private ListView listThreads;
    private ThreadsListAdapter adapter;
    private ActivityListener activityListener;
    private ProgressBar progressBar;
    private UIUpdater uiUpdater;

    public static ThreadsFragment newInstance() {
        ThreadsFragment f = new ThreadsFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreateView");
        init(inflater);
        initToast();

        loadDataOnBackground();

        return mainView;
    }

    private void init(LayoutInflater inflater){
        mainView = inflater.inflate(R.layout.chat_sdk_activity_threads, null);
        initViews();
    }

    @Override
    public void initViews() {
        listThreads = (ListView) mainView.findViewById(R.id.list_threads);
        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progress_bar);
        initList();
    }

    private void initList(){
        adapter = new ThreadsListAdapter(getActivity());
        listThreads.setAdapter(adapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) Log.i(TAG, "Thread Selected: " + adapter.getItem(position).getName()
                        + ", ID: " + adapter.getItem(position).getEntityId());
                startChatActivityForID(adapter.getItem(position).getId());
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Public);

        adapter.setListData(ThreadsListAdapter.ThreadListItem.makeList(threads));

        if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );
    }

    @Override
    public void loadDataOnBackground() {
        super.loadDataOnBackground();

        if (DEBUG) Log.v(TAG, "loadDataOnBackground");

        if (mainView == null)
            return;

        final boolean isFirst;
        if (uiUpdater != null)
        {
            isFirst = false;
            uiUpdater.setKilled(true);
            ChatSDKThreadPool.getInstance().removeSchedule(uiUpdater);
        }
        else
        {
            isFirst = true;
        }

        final boolean noItems = adapter != null && adapter.getListData().size() == 0;
        if (isFirst && noItems) {
            listThreads.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        uiUpdater = new UIUpdater() {
            @Override
            public void run() {

                if (isKilled() && !isFirst && noItems)
                    return;

//                List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Public);

//                if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );

                Message message = new Message();
                message.what = 1;
                message.obj = BNetworkManager.sharedManager().getNetworkAdapter().threadItemsWithType(BThread.Type.Public);

                handler.sendMessage(message);

                uiUpdater = null;
            }
        };

        ChatSDKThreadPool.getInstance().scheduleExecute(uiUpdater, noItems && isFirst ? 0 : isFirst ? 1 : 4);
    }

    @Override
    public void refreshForEntity(Entity entity) {
        super.refreshForEntity(entity);
        adapter.replaceOrAddItem((BThread) entity);
    }

    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:
                    adapter.setListData((List<ThreadsListAdapter.ThreadListItem>) msg.obj);
                    progressBar.setVisibility(View.GONE);
                    listThreads.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add Public chat Room");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add)
        {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogUtils.ChatSDKEditTextDialog dialog = DialogUtils.ChatSDKEditTextDialog.getInstace();

            dialog.setTitleAndListen("Add Chat Room", new DialogUtils.ChatSDKEditTextDialog.EditTextDialogInterface() {
                @Override
                public void onFinished(final String s) {
                    if (DEBUG) Log.v(TAG, "onFinished, Thread Name: " + s);

                    showProgDialog("Creating thread...");
                    BNetworkManager.sharedManager().getNetworkAdapter().createPublicThreadWithName(s, new CompletionListenerWithDataAndError<BThread, Object>() {
                        @Override
                        public void onDone(final BThread bThread) {

                            // Add the current user to the thread.
                            BNetworkManager.sharedManager().getNetworkAdapter().addUsersToThread(bThread,
                                    new RepetitiveCompletionListenerWithError<BUser, Object>() {
                                @Override
                                public boolean onItem(BUser user) {

                                    return false;
                                }

                                @Override
                                public void onDone() {
                                    dismissProgDialog();
                                    adapter.addRow(bThread);
                                    showToast("Public thread " + s + " is created.");
                                }

                                @Override
                                public void onItemError(BUser user, Object o) {

                                }
                            }, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());

                        }

                        @Override
                        public void onDoneWithError(BThread bThread, Object o) {
                            showToast("Failed to create public thread " + s + ".");
                        }
                    });
                }
            });

            dialog.show(fm, "Add Public Chat Dialog");


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        BatchedEvent batchedEvents = new BatchedEvent(APP_EVENT_TAG, "", Event.Type.AppEvent, handler);

        batchedEvents.setBatchedAction(Event.Type.MessageEvent, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                if (DEBUG) Log.v(TAG, "onMessageReceived");
                for (String messageID : list)
                {
                    BMessage message = DaoCore.fetchEntityWithEntityID(BMessage.class, messageID);
                    if (message.getBThreadOwner().getType() == BThread.Type.Public)
                    {
                        loadDataOnBackground();
                        return;
                    }
                }
            }
        });

        batchedEvents.setBatchedAction(Event.Type.ThreadEvent, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                if (DEBUG) Log.v(TAG, "onThreadDetailsChanged");
                for (String threadId : list)
                {
                    BThread thread = DaoCore.<BThread>fetchEntityWithEntityID(BThread.class, threadId);
                    if (thread.getType() != null && thread.getType() == BThread.Type.Public)
                    {
                        loadDataOnBackground();
                        return;
                    }
                }
            }
        });

        batchedEvents.setBatchedAction(Event.Type.UserEvent, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                if (DEBUG) Log.v(TAG, "onUserDetailsChange");
                loadDataOnBackground();
            }
        });

        EventManager.getInstance().removeEventByTag(APP_EVENT_TAG);
        EventManager.getInstance().addAppEvent(batchedEvents);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
       /* BNetworkManager.sharedManager().getNetworkAdapter().removeActivityListener(activityListener);*/
    }
}