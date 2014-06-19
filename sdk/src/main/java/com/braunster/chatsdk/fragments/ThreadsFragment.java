package com.braunster.chatsdk.fragments;

import android.content.Intent;
import android.os.Bundle;
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

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.List;

/**
 * Created by itzik on 6/17/2014.
 */
public class ThreadsFragment extends BaseFragment {

    //TODO add selection of thread type to see.

    private static final String TAG = ThreadsFragment.class.getSimpleName();
    private static boolean DEBUG = true;

    private ListView listThreads;
    private ThreadsListAdapter listAdapter;
    private ActivityListener activityListener;

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

        init(inflater);

        return mainView;
    }

    private void init(LayoutInflater inflater){
        mainView = inflater.inflate(R.layout.chat_sdk_activity_threads, null);
        initViews();
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
                if (DEBUG) Log.i(TAG, "Thread Selected: " + listAdapter.getItem(position).getName()
                        + ", ID: " + listAdapter.getItem(position).getEntityID());

                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.THREAD_ID, listAdapter.getItem(position).getEntityID());

                startActivity(intent);
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        List<BThread> threads = BNetworkManager.getInstance().threadsWithType(BThread.Type.Public);

        listAdapter.setListData(threads);
        if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_add_chat_room, 10, "Add Chat");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(android.R.drawable.ic_menu_add);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_add_chat_room)
        {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            DialogUtils.EditTextDialog dialog = DialogUtils.EditTextDialog.getInstace();

            dialog.setTitleAndListen("Add Chat Room", new DialogUtils.EditTextDialog.EditTextDialogInterface() {
                @Override
                public void onFinished(String s) {
                    if (DEBUG) Log.v(TAG, "onFinished, Thread Name: " + s);
                    // TODO add new thread to the database and server.
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

        activityListener = BNetworkManager.getInstance().addActivityListener(new ActivityListener() {
            @Override
            public void onThreadAdded(BThread thread) {
                if (DEBUG) Log.d(TAG, "Thread is added");
                listAdapter.addRow(thread);
            }

            @Override
            public void onMessageAdded(BMessage message) {
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BNetworkManager.getInstance().removeActivityListener(activityListener);
    }
}