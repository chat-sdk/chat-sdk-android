/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.fragments;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.braunster.chatsdk.adapter.ChatSDKThreadsListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.events.BatchedEvent;
import com.braunster.chatsdk.network.events.Event;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.Batcher;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.UIUpdater;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.List;

import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKThreadsFragment extends ChatSDKBaseFragment {

    private static final String TAG = ChatSDKThreadsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ThreadsFragment;
    public static final String APP_EVENT_TAG= "ChatRoomsFrag";

    private ListView listThreads;
    private ChatSDKThreadsListAdapter adapter;
    private ProgressBar progressBar;
    private UIUpdater uiUpdater;

    public static ChatSDKThreadsFragment newInstance() {
        return new ChatSDKThreadsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        init(inflater);

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
        adapter = new ChatSDKThreadsListAdapter(getActivity());
        listThreads.setAdapter(adapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatActivityForID(adapter.getItem(position).getId());
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        adapter.setThreadItems(BNetworkManager.sharedManager().getNetworkAdapter().threadItemsWithType(BThread.Type.Public, adapter.getItemMaker()));
    }

    @Override
    public void loadDataOnBackground() {
        super.loadDataOnBackground();

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

        final boolean noItems = adapter != null && adapter.getThreadItems().size() == 0;
        if (isFirst && noItems) {
            listThreads.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        uiUpdater = new UIUpdater() {
            @Override
            public void run() {

                if (isKilled() && !isFirst && noItems)
                    return;

                Message message = new Message();
                message.what = 1;
                message.obj = BNetworkManager.sharedManager().getNetworkAdapter().threadItemsWithType(BThread.Type.Public, adapter.getItemMaker());

                handler.sendMessageAtFrontOfQueue(message);

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
                    adapter.setThreadItems((List<ChatSDKThreadsListAdapter.ThreadListItem>) msg.obj);
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
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, getString(R.string.public_thread_fragment_add_item_text));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add)
        {
            FragmentManager fm = getActivity().getFragmentManager();
            final DialogUtils.ChatSDKEditTextDialog dialog = DialogUtils.ChatSDKEditTextDialog.getInstace();

            dialog.setTitleAndListen( getString(R.string.add_public_chat_dialog_title), new DialogUtils.ChatSDKEditTextDialog.EditTextDialogInterface() {
                @Override
                public void onFinished(final String s) {

                    showProgDialog(getString(R.string.add_public_chat_dialog_progress_message));
                    BNetworkManager.sharedManager().getNetworkAdapter().createPublicThreadWithName(s)
                            .done(new DoneCallback<BThread>() {
                                @Override
                                public void onDone(final BThread thread) {
                                    // Add the current user to the thread.
                                    getNetworkAdapter().addUsersToThread(thread, BNetworkManager.sharedManager().getNetworkAdapter().currentUserModel())
                                            .done(new DoneCallback<BThread>() {
                                                @Override
                                                public void onDone(BThread thread) {
                                                    dismissProgDialog();
                                                    adapter.addRow(thread);
                                                    showToast( getString(R.string.add_public_chat_dialog_toast_success_before_thread_name)
                                                            + s
                                                            + getString(R.string.add_public_chat_dialog_toast_success_after_thread_name) ) ;
                                                }
                                            });
                                }
                            })
                            .fail(new FailCallback<BError>() {
                                @Override
                                public void onFail(BError bError) {
                                    showAlertToast(getString(R.string.add_public_chat_dialog_toast_error_before_thread_name) + s);

                                    if (DEBUG) Timber.e("Error: %s", bError.message);

                                    dismissProgDialog();
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

        batchedEvents.setBatchedAction(Event.Type.AppEvent, 3000, new Batcher.BatchedAction<String>() {
            @Override
            public void triggered(List<String> list) {
                loadDataOnBackground();
            }
        });

        getNetworkAdapter().getEventManager().removeEventByTag(APP_EVENT_TAG);
        getNetworkAdapter().getEventManager().addEvent(batchedEvents);
    }
}