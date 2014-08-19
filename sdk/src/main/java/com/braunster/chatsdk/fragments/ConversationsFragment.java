package com.braunster.chatsdk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.TimingLogger;
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
import com.braunster.chatsdk.activities.PickFriendsActivity;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.UIUpdater;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ConversationsFragment extends BaseFragment {

    // TODO multiselect of contacts to start chatting with.

    private static final String TAG = ConversationsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ConversationsFragment;
    private ListView listThreads;
    private ThreadsListAdapter adapter;
    private ProgressBar progressBar;
    private BUser user;

    private UIUpdater uiUpdater;

    public static ConversationsFragment newInstance() {
        ConversationsFragment f = new ConversationsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
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

        mainView = inflater.inflate(R.layout.chat_sdk_activity_threads, null);

        initViews();

        initToast();

        loadDataOnBackground();

        return mainView;
    }

    @Override
    public void initViews() {
        listThreads = (ListView) mainView.findViewById(R.id.list_threads);
        progressBar = (ProgressBar) mainView.findViewById(R.id.progress_bar);
        initList();
    }

    private void initList(){

        adapter = new ThreadsListAdapter(getActivity());
        listThreads.setAdapter(adapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) Log.i(TAG, "Thread Selected: " + adapter.getItem(position).getName()
                        + ", ID: " + adapter.getItem(position).getEntityId() );
                startChatActivityForID(adapter.getItem(position).getId());
            }
        });

        listThreads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG)
                    Log.i(TAG, "Thread Long Selected: " + adapter.getItem(position).getName()
                            + ", ID: " + adapter.getItem(position).getEntityId());

                showAlertDialog("", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                        getResources().getString(R.string.cancel), null, new DeleteThread(adapter.getItem(position).getEntityId()));

                return true;
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Private);

        adapter.setListData(ThreadsListAdapter.ThreadListItem.makeList(threads));

        if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );
    }

    TimingLogger timings;

    @Override
    public void loadDataOnBackground() {
        super.loadDataOnBackground();

        if (DEBUG) Log.d(TAG, "isLoggable: " + Log.isLoggable(TAG, Log.VERBOSE));

        if (DEBUG) timings = new TimingLogger(TAG, "loadDataOnBackground");

        if (DEBUG) Log.v(TAG, "loadDataOnBackground");

        if (mainView == null)
        {
            if (DEBUG) Log.e(TAG, "Main view is null");
            return;
        }

        if (uiUpdater == null && adapter != null && adapter.getListData().size() == 0) {
            if (DEBUG) Log.v(TAG, "loadDataOnBackground, hiding list.");
            listThreads.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        if (uiUpdater != null)
            uiUpdater.setKilled(true);

        uiUpdater = new UIUpdater() {
            @Override
            public void run() {
                if (isKilled())
                {
                    if (DEBUG) Log.v(TAG, "uiUpdater, is killed.");
                    return;
                }

                List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Private);
                if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );

                if (DEBUG) timings.addSplit("Loading threads");

                Message message = new Message();
                message.what = 1;

                List<ThreadsListAdapter.ThreadListItem> list =ThreadsListAdapter.ThreadListItem.makeList(threads);
                if (DEBUG) {Log.d(TAG, "MakList - Before"); timings.addSplit("Making list.");}

                message.obj = list;
                handler.sendMessage(message);

                if (DEBUG) timings.addSplit("Sending message to handler.");

            }
        };

        ChatSDKThreadPool.getInstance().execute(uiUpdater);
    }

    @Override
    public void refreshForEntity(Entity entity) {
        super.refreshForEntity(entity);
        if (adapter.getCount() == 0)
            return;;

        adapter.replaceOrAddItem((BThread) entity);
        if (listThreads.getVisibility() == View.INVISIBLE)
        {
            progressBar.setVisibility(View.INVISIBLE);
            listThreads.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void clearData() {
        if (adapter != null)
        {
            adapter.getListData().clear();
            adapter.notifyDataSetChanged();
        }
    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:
                    timings.addSplit("Updating UI");
                    adapter.setListData((List<ThreadsListAdapter.ThreadListItem>) msg.obj);
                    progressBar.setVisibility(View.INVISIBLE);
                    listThreads.setVisibility(View.VISIBLE);
                    timings.dumpToLog();
                    timings.reset(TAG, "loadDataOnBackground");
                    break;
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add Conversation");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.ic_plus);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_add)
        {
            Intent intent = new Intent(getActivity(), PickFriendsActivity.class);

            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
