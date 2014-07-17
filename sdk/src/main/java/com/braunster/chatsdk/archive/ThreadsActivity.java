package com.braunster.chatsdk.archive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.adapter.ThreadsListAdapter;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.network.BNetworkManager;

import java.util.List;

/**
 * Created by itzik on 6/8/2014.
 */
public class ThreadsActivity extends ActionBarActivity {


    //TODO add selection of thread type to see.
    private static final String TAG = ThreadsActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private ListView listThreads;
    private ThreadsListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activity_threads);

        initViews();
    }

    private void initViews() {
        listThreads = (ListView) findViewById(R.id.list_threads);

        initList();
    }

    private void initList(){
        List<BThread> threads = BNetworkManager.sharedManager().getNetworkAdapter().threadsWithType(BThread.Type.Private);

        if (DEBUG) Log.d(TAG, "Threads, Amount: " + threads.size());

        listAdapter = new ThreadsListAdapter(this, ThreadsListAdapter.ThreadListItem.makeList(threads));
        listThreads.setAdapter(listAdapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (DEBUG) Log.i(TAG, "Thread Selected: " + listAdapter.getItem(position).getName()
                        + ", ID: " + listAdapter.getItem(position).getEntityId());

                Intent intent = new Intent(ThreadsActivity.this, ChatActivity.class);
                intent.putExtra(ChatActivity.THREAD_ID, listAdapter.getItem(position).getEntityId());

                startActivity(intent);
            }
        });
    }
}
