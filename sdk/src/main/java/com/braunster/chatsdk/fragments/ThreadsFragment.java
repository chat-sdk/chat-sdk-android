package com.braunster.chatsdk.fragments;

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
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.ActivityListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithError;
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
        if (DEBUG) Log.d(TAG, "onCreateView");
        init(inflater);

        loadData();

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
                startChatActivityForID(listAdapter.getItem(position).getId());
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Public);

        listAdapter.setListData(threads);
        if (DEBUG) Log.d(TAG, "Threads, Amount: " + (threads != null ? threads.size(): "No Threads") );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_add, 10, "Add Public chat Room");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(android.R.drawable.ic_menu_add);
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
                    BNetworkManager.sharedManager().getNetworkAdapter().createPublicThreadWithName(s, new CompletionListenerWithDataAndError<BThread, Object>() {
                        @Override
                        public void onDone(BThread bThread) {

                            // Add the current user to the thread.
                            BNetworkManager.sharedManager().getNetworkAdapter().addUsersToThread(bThread,
                                    new RepetitiveCompletionListenerWithError<BUser, Object>() {
                                @Override
                                public boolean onItem(BUser user) {

                                    return false;
                                }

                                @Override
                                public void onDone() {

                                }

                                @Override
                                public void onItemError(BUser user, Object o) {

                                }
                            }, BNetworkManager.sharedManager().getNetworkAdapter().currentUser());

                            listAdapter.addRow(bThread);
                            Toast.makeText(getActivity(), "Public thread " + s + " is created.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onDoneWithError(BThread bThread, Object o) {
                            Toast.makeText(getActivity(), "Failed to create public thread " + s + ".", Toast.LENGTH_SHORT).show();
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

        /*activityListener = BNetworkManager.sharedManager().getNetworkAdapter().addActivityListener(new ActivityListener() {
            @Override
            public void onThreadDetailsChanged(BThread thread) {
                if (DEBUG) Log.d(TAG, "Thread is added");
                listAdapter.addRow(thread);
            }

            @Override
            public void onMessageAdded(BMessage message) {
            }
        });*/
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