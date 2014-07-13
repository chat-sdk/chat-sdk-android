package com.braunster.chatsdk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.activities.PickFriendsActivity;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ConversationsFragment extends BaseFragment {

    // TODO multiselect of contacts to start chatting with.

    private static final String TAG = ConversationsFragment.class.getSimpleName();
    private static boolean DEBUG = true;
    private ListView listThreads;
    private ThreadsListAdapter listAdapter;
    private BUser user;

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

        loadData();

        return mainView;
    }

    @Override
    public void initViews() {
        listThreads = (ListView) mainView.findViewById(R.id.list_threads);

        initList();
    }

    private void initList(){

        listAdapter = new ThreadsListAdapter(getActivity());
        listThreads.setAdapter(listAdapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) Log.i(TAG, "Thread Selected: " + listAdapter.getItem(position).displayName()
                        + ", ID: " + listAdapter.getItem(position).getEntityID());
                startChatActivityForID(listAdapter.getItem(position).getId());
            }
        });

        listThreads.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG)
                    Log.i(TAG, "Thread Long Selected: " + listAdapter.getItem(position).displayName()
                            + ", ID: " + listAdapter.getItem(position).getEntityID());

                showAlertDialog("", getResources().getString(R.string.alert_delete_thread), getResources().getString(R.string.delete),
                        getResources().getString(R.string.cancel), null, new DeleteThread(listAdapter.getItem(position).getEntityID()));

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

        listAdapter.setListData(threads);

        if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );
    }

    @Override
    public void loadDataOnBackground() {
        super.loadDataOnBackground();
        if (DEBUG) Log.v(TAG, "loadDataOnBackground");

        if (mainView == null)
            return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Private);

                if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );

                Message message = new Message();
                message.what = 1;
                message.obj = threads;

                handler.sendMessage(message);
            }
        }).start();

    }

    Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 1:
                    listAdapter.setListData((List<BThread>) msg.obj);
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
        item.setIcon(android.R.drawable.ic_menu_add);
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
