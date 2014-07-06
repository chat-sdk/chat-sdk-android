package com.braunster.chatsdk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.braunster.chatsdk.activities.ChatActivity;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BNetworkManager;

/**
 * Created by itzik on 6/17/2014.
 */
public class BaseFragment extends DialogFragment implements BaseFragmentInterface {

    private static final String TAG = BaseFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    View mainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
//        mainView = inflater.inflate(resourceID, null);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        loadData();
        // TODO handle network call more efficiantly check time intervals and mabey listen to data coming.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void refresh() {
        loadData();
    }

    @Override
    public void loadData() {

    }

    @Override
    public void initViews() {

    }

    void startChatActivityForID(long id){
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.THREAD_ID, id);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    void createAndOpenThreadWithUsers(String name, BUser...users){
        BNetworkManager.sharedManager().getNetworkAdapter().createThreadWithUsers(name, new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object>() {

            BThread thread = null;

            @Override
            public boolean onMainFinised(BThread bThread, Object o) {
                if (o != null)
                {
                    Toast.makeText(getActivity(), "Failed to start chat.", Toast.LENGTH_SHORT).show();
                    return true;
                }

                if (DEBUG) Log.d(TAG, "New thread is created.");

                thread = bThread;

                return false;
            }

            @Override
            public boolean onItem(BUser item) {
                return false;
            }

            @Override
            public void onDone() {
                Log.d(TAG, "On done.");
                if (thread != null)
                {
                    startChatActivityForID(thread.getId());
                }
            }

            @Override
            public void onItemError(BUser user, Object o) {
                if (DEBUG) Log.d(TAG, "Failed to add user to thread, User name: " +user.getName());
            }
        }, users);
    }
}

interface BaseFragmentInterface{
    public void refresh();

    public void loadData();

    public void initViews();
}
