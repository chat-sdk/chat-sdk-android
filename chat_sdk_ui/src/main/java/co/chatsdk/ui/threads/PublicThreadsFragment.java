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
import android.widget.Toast;

import co.chatsdk.core.InterfaceManager;
import co.chatsdk.core.NM;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.CompletableSource;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.ui.helpers.DialogUtils;


import io.reactivex.functions.Function;
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

    public void initViews() {
        listThreads = (ListView) mainView.findViewById(R.id.list_threads);
        initList();
    }

    private void initList(){
        adapter = new ThreadsListAdapter((AppCompatActivity) getActivity());
        listThreads.setAdapter(adapter);

        listThreads.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InterfaceManager.shared().a.startChatActivityForID(adapter.getItem(position).getEntityID());
            }
        });
    }

    public void loadData() {

        if (mainView == null) {
            return;
        }

        adapter.setAllItems(NM.thread().getThreads(ThreadType.Public));
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
                public void onFinished(final String threadName) {

                    showOrUpdateProgressDialog(getString(R.string.add_public_chat_dialog_progress_message));

                    NM.publicThread().createPublicThreadWithName(threadName).flatMapCompletable(new Function<Thread, CompletableSource>() {
                        @Override
                        public CompletableSource apply(@NonNull final Thread thread) throws Exception {
                            return  NM.thread().addUsersToThread(thread, NM.currentUser()).doOnError(new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable throwable) throws Exception {
                                    throwable.printStackTrace();
                                    Toast.makeText(PublicThreadsFragment.this.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    dismissProgressDialog();
                                }
                            }).doOnComplete(new Action() {
                                @Override
                                public void run() throws Exception {
                                    dismissProgressDialog();
                                    adapter.addRow(thread);

                                    // TODO: Improve this
                                    ToastHelper.show(getString(R.string.add_public_chat_dialog_toast_success_before_thread_name) + threadName + getString(R.string.add_public_chat_dialog_toast_success_after_thread_name) ); ;
                                }
                            });
                        }
                    }).observeOn(AndroidSchedulers.mainThread()).subscribe();
                }
            });

            // TODO: Localize
            dialog.show(fm, "Add Public Chat Dialog");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
    }
}