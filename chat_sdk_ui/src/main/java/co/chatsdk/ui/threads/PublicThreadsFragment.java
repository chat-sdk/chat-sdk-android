/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.threads;

import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.ui.helpers.DialogUtils;


import timber.log.Timber;

/**
 * Created by itzik on 6/17/2014.
 */
public class PublicThreadsFragment extends BaseFragment {

    private static final String TAG = PublicThreadsFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ThreadsFragment;
    public static final String APP_EVENT_TAG= "ChatRoomsFrag";

    private ListView listThreads;
    private ThreadsListAdapter adapter;
    private ProgressBar progressBar;
//    private UIUpdater uiUpdater;

    public static PublicThreadsFragment newInstance() {
        return new PublicThreadsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        init(inflater);

        NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType((EventType.MessageAdded)))
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(NetworkEvent networkEvent) throws Exception {
                loadData();
            }
        });

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
        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progress_bar);
        initList();
    }

    private void initList(){
        adapter = new ThreadsListAdapter((AppCompatActivity) getActivity());
        listThreads.setAdapter(adapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startChatActivityForThreadID(adapter.getItem(position).getId());
            }
        });
    }

    @Override
    public void loadData() {
        super.loadData();

        if (mainView == null)
            return;

        adapter.setAllItems(NM.thread().getThreads(ThreadType.Public));
    }

    @Override
    public void refreshForEntity(Object entity) {
        super.refreshForEntity(entity);
        adapter.replaceOrAddItem((Thread) entity);
    }

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
            FragmentManager fm = getActivity().getSupportFragmentManager();
            final DialogUtils.ChatSDKEditTextDialog dialog = DialogUtils.ChatSDKEditTextDialog.getInstace();

            dialog.setTitleAndListen( getString(R.string.add_public_chat_dialog_title), new DialogUtils.ChatSDKEditTextDialog.EditTextDialogInterface() {
                @Override
                public void onFinished(final String s) {

                    showOrUpdateProgressDialog(getString(R.string.add_public_chat_dialog_progress_message));

                    NM.publicThread().createPublicThreadWithName(s)
                            .doOnSuccess(new Consumer<Thread>() {
                                @Override
                                public void accept(final Thread thread) throws Exception {
                                    // Add the current user to the thread.
                                    // TODO: Check if this is needed - maybe we add the user when the chat view opens
                                    NM.thread().addUsersToThread(thread, NM.currentUser())
                                            .doOnComplete(new Action() {
                                                @Override
                                                public void run() throws Exception {
                                                    dismissProgressDialog();
                                                    adapter.addRow(thread);
                                                    showToast( getString(R.string.add_public_chat_dialog_toast_success_before_thread_name)
                                                            + s
                                                            + getString(R.string.add_public_chat_dialog_toast_success_after_thread_name) ) ;
                                                }
                                            }).subscribe();
                                }
                            })
                            .doOnError(new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) throws Exception {
                                    if (DEBUG) Timber.e("Error: %s", throwable.getMessage());
                                    showToast(getString(R.string.add_public_chat_dialog_toast_error_before_thread_name) + s);
                                    dismissProgressDialog();
                                }
                            }).subscribe();
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

        // TODO: Check this
//        BatchedEvent batchedEvents = new BatchedEvent(APP_EVENT_TAG, "", Event.Type.AppEvent, handler);
//
//        batchedEvents.setBatchedAction(Event.Type.AppEvent, 3000, new Batcher.BatchedAction<String>() {
//            @Override
//            public void triggered(List<String> list) {
//                loadDataOnBackground();
//            }
//        });
//
//        NetworkManager.getCoreInterface().getEventManager().removeEventByTag(APP_EVENT_TAG);
//        NetworkManager.getCoreInterface().getEventManager().addEvent(batchedEvents);
    }
}