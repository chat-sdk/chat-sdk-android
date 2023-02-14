package sdk.chat.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.jakewharton.rxrelay2.PublishRelay;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.interfaces.SearchSupported;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.performance.ThreadHoldersDiffCallback;
import sdk.chat.ui.provider.MenuItemProvider;
import sdk.chat.ui.view_holders.ThreadViewHolder;
import sdk.guru.common.DisposableMap;
import sdk.guru.common.RX;

public abstract class ThreadsFragment extends BaseFragment implements SearchSupported {

    protected String filter;
    protected boolean startingChat = false;

    protected DialogsListAdapter<ThreadHolder> dialogsListAdapter;

    protected PublishRelay<Thread> onClickPublishRelay = PublishRelay.create();
    protected PublishRelay<Thread> onLongClickPublishRelay = PublishRelay.create();

    protected List<ThreadHolder> threadHolders = new Vector<>();

    protected DialogsList dialogsList;
    protected RelativeLayout root;

    protected DisposableMap keep = new DisposableMap();

    protected boolean listenersAdded = false;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_threads;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keep.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(
                        EventType.Logout
                )).subscribe(networkEvent -> {
                    clearData();
                }));

    }

    @Override
    public void onStart() {
        super.onStart();
        addListeners();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        dialogsList = view.findViewById(R.id.dialogsList);
        root = view.findViewById(R.id.root);

        initViews();
        hideKeyboard();


        return view;
    }

    public boolean addListeners() {

        if (listenersAdded) {
            return false;
        }
        listenersAdded = true;

        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(NetworkEvent.filterType(
                EventType.ThreadMetaUpdated,
                EventType.TypingStateUpdated,
                EventType.UserMetaUpdated,
                EventType.UserPresenceUpdated,
                EventType.MessageReadReceiptUpdated,
                EventType.MessageUpdated,
                EventType.ThreadUserAdded,
                EventType.ThreadUserRemoved,
                EventType.ThreadUserUpdated,
                EventType.ThreadRead
            ))
            .subscribe(networkEvent -> {
                root.post(() -> {
                    synchronize(false);
                });
            }));

        dm.add(ChatSDK.events().sourceOnSingle().filter(NetworkEvent.filterType(
            EventType.ThreadsUpdated
        )).subscribe(networkEvent -> {
            loadData();
        }));

        dm.add(ChatSDK.events().sourceOnSingle().filter(NetworkEvent.filterType(
            EventType.ThreadAdded
        )).subscribe(networkEvent -> {
            root.post(() -> {
                addThread(networkEvent.getThread(), true, true);
            });
        }));

        dm.add(ChatSDK.events().sourceOnSingle().filter(NetworkEvent.filterType(
            EventType.ThreadRemoved
        )).subscribe(networkEvent -> {
            root.post(() -> {
                removeThread(networkEvent.getThread());
            });
        }));

        dm.add(ChatSDK.events().sourceOnSingle().filter(NetworkEvent.filterType(
            EventType.MessageAdded,
            EventType.MessageRemoved,
            EventType.ThreadMessagesUpdated
        )).subscribe(networkEvent -> {
            root.post(() -> {

                if (networkEvent.typeIs(EventType.MessageAdded)) {
                    Logger.debug("ThreadsFragment Message Added: " + networkEvent.getMessage().getText());
                }

                if (!addThread(networkEvent.getThread(), true, true)) {
                    synchronize(true);
                }
            });
        }));

        return true;
    }

    public void removeListeners() {
        listenersAdded = false;
        dm.dispose();
    }

    public void initViews() {

//        if (!useAsyncAdapter) {
            dialogsListAdapter = new DialogsListAdapter<>(R.layout.view_holder_thread, ThreadViewHolder.class, null);
//        } else {
//            asyncDialogsListAdapter = new AsyncDialogsListAdapter(R.layout.view_holder_thread, ThreadViewHolder.class, loader);
//        }

        if (UIModule.config().getThreadTimeFormat() != null) {
            dialogsListAdapter().setDatesFormatter(date -> {
                return DateFormatter.format(date, UIModule.config().getThreadTimeFormat());
            });
        }



        dialogsList.setAdapter(dialogsListAdapter());

//                Sometimes a new group is not registered
//                Create new thread not ordered properly
//                when a new message is received on a group, it has no avatar (fixed)


        // Stop the image from flashing when the list is reloaded

        // TODO: Thread
//        RecyclerView.ItemAnimator animator = dialogsList.getItemAnimator();
//        if (animator instanceof SimpleItemAnimator) {
//            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
//        }

        dialogsList.setItemAnimator(null);

        dialogsListAdapter().setOnDialogViewClickListener((view, dialog) -> {
            dialog.markRead();
            startChatActivity(dialog.getId());
        });
        dialogsListAdapter().setOnDialogLongClickListener(dialog -> {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(dialog.getId());
            if (thread != null) {
                onLongClickPublishRelay.accept(thread);
            }
        });
    }

    protected void startChatActivity(String threadEntityID) {
        if (!startingChat) {
            startingChat = true;
            ChatSDK.ui().startChatActivityForID(getContext(), threadEntityID);
        }
    }

    protected abstract Predicate<NetworkEvent> mainEventFilter();

    protected boolean allowThreadCreation() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        ChatSDKUI.provider().menuItems().addAddItem(getContext(), menu, 1);
        
        if (!allowThreadCreation()) {
            menu.removeItem(MenuItemProvider.addItemId);
        }
    }

    // Override this in the subclass to handle the plus button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        startingChat = false;
        if (threadHolders.isEmpty()) {
            dm.add(loadInitialData().subscribe(() -> {
                addListeners();
            }));
        } else {
            loadData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void clearData() {
        threadHolders.clear();
        if (dialogsListAdapter != null) {
            dialogsListAdapter.clear();
            synchronize(true);
        }
//        synchronize(true);
//        if (asyncDialogsListAdapter != null) {
//            asyncDialogsListAdapter.submitList(new ArrayList<>());
//        }
//        threadHolderHashMap.clear();
    }

    public DialogsListAdapter<ThreadHolder> dialogsListAdapter() {
        if (dialogsListAdapter != null) {
            return dialogsListAdapter;
        }
//        if (asyncDialogsListAdapter != null) {
//            return asyncDialogsListAdapter;
//        }
        return null;
    }

    public void setTabVisibility(boolean isVisible) {
        super.setTabVisibility(isVisible);
        if (isVisible) {
            synchronize(true);
        }
    }

    @Override
    public void reloadData() {
        loadData();
    }

    public void loadData() {
        if (dialogsListAdapter() != null) {
//            if (!didLoadData) {
                getThreads().map(threads -> {
                    threads = filter(threads);

                    threadHolders.clear();
                    for (Thread thread : threads) {
                        addThread(thread, false, false);
                    }
                    return threadHolders;
                }).observeOn(RX.single()).observeOn(RX.main()).doOnSuccess(threadHolders -> {
                    synchronize(true);
                }).subscribe();

//                didLoadData = true;
//            } else {
//                synchronize(true);
//            }
        }
    }

    public Completable loadInitialData() {
        return Completable.defer((Callable<CompletableSource>) () -> {
            if (dialogsListAdapter() != null) {
                return getThreads().map(threads -> {
                    for (Thread thread : threads) {
                        addThread(thread, false, false);
                    }
                    return threadHolders;
                }).observeOn(RX.single()).observeOn(RX.main()).doOnSuccess(threadHolders -> {
                    synchronize(true);
                }).ignoreElement();
            } else {
                return Completable.complete();
            }
        });
    }

    public boolean addThread(Thread thread, boolean sort, boolean sync) {
//        if (!threadHolderExists(thread)) {

        ThreadHolder holder = getOrCreateThreadHolder(thread);
        if (!threadHolders.contains(holder)) {
            threadHolders.add(holder);
            if (sort) {
                sortThreadHolders();
            }
            if (sync) {
                synchronize(false);
            }
            return true;
        }
        return false;
    }

    // Synchronize the thread holders with the list
    protected void synchronize(boolean sort) {

        if (sort) {
            sortThreadHolders();
        }

        long start = System.currentTimeMillis();

        List<ThreadHolder> newHolders = new ArrayList<>(threadHolders);
        if (dialogsListAdapter != null) {

            final List<ThreadHolder> oldHolders = new ArrayList<>(dialogsListAdapter.getItems());

//            RX.single().scheduleDirect(() -> {

                ThreadHoldersDiffCallback callback = new ThreadHoldersDiffCallback(newHolders, oldHolders);
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

//                dialogsList.post(() -> {
                    dialogsListAdapter.getItems().clear();
                    dialogsListAdapter.setItems(newHolders);
                    result.dispatchUpdatesTo(dialogsListAdapter);
//                });

//            });

        }
//        if (asyncDialogsListAdapter != null) {
//            asyncDialogsListAdapter.submitList(newHolders);
//        }

        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println("Diff: " + diff);

    }

    public void updateThread(final Thread thread) {
        final ThreadHolder holder = ChatSDKUI.provider().holderProvider().getOrCreateThreadHolder(thread);
        if (holder != null) {
            holder.update();
            synchronize(false);
        }
    }

    public void sortThreadHolders() {
        Collections.sort(threadHolders, (o1, o2) -> {
            if (!o1.getWeight().equals(o2.getWeight())) {
                return o1.getWeight().compareTo(o2.getWeight());
            }
            return o2.getDate().compareTo(o1.getDate());
        });
    }

    public ThreadHolder getOrCreateThreadHolder(Thread thread) {
        return ChatSDKUI.provider().holderProvider().getOrCreateThreadHolder(thread);
    }

    public void removeThread(Thread thread) {
        ThreadHolder holder = ChatSDKUI.provider().holderProvider().getThreadHolder(thread);
        if (holder != null) {
            threadHolders.remove(holder);
            synchronize(false);
        }
    }

    protected abstract Single<List<Thread>> getThreads();

    public List<Thread> filter(List<Thread> threads) {
        if (filter == null || filter.isEmpty()) {
            return threads;
        }

        List<Thread> filteredThreads = new ArrayList<>();
        for (Thread t : threads) {
            if (t.getName() != null && t.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredThreads.add(t);
            } else {
                for (User u : t.getUsers()) {
                    if (u.getName() != null && u.getName().toLowerCase().contains(filter.toLowerCase())) {
                        filteredThreads.add(t);
                        break;
                    }
                }
            }
        }
        return filteredThreads;
    }

    public Observable<Thread> getOnLongClickObservable() {
        return onLongClickPublishRelay.hide();
    }

    public void filter(String text) {
        filter = text;
        loadData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//        batcher.dispose();
    }
}
