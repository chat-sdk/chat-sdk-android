package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.fragments.abstracted.ChatSDKAbstractConversationsFragment;

/**
 * Created by itzik on 6/17/2014.
 */
public class ChatSDKConversationsFragment extends ChatSDKAbstractConversationsFragment {

    private static final String TAG = ChatSDKConversationsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ConversationsFragment;

    public static ChatSDKConversationsFragment newInstance() {
        ChatSDKConversationsFragment f = new ChatSDKConversationsFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(inflateMenuItems);
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

}
