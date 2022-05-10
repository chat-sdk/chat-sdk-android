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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.interfaces.SearchSupported;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.provider.MenuItemProvider;
import sdk.chat.ui.utils.GlideWith;
import sdk.chat.ui.utils.ThreadImageBuilder;
import sdk.chat.ui.view_holders.ThreadViewHolder;
import sdk.guru.common.RX;

public abstract class ThreadsFragment extends BaseFragment implements SearchSupported {

    protected String filter;
//    protected EventBatcher batcher;
//    protected boolean reloadDataOnResume = true;

    protected DialogsListAdapter<ThreadHolder> dialogsListAdapter;
    protected Map<Thread, ThreadHolder> threadHolderHashMap = new HashMap<>();

    protected PublishRelay<Thread> onClickPublishRelay = PublishRelay.create();
    protected PublishRelay<Thread> onLongClickPublishRelay = PublishRelay.create();

    protected List<ThreadHolder> threadHolders = new Vector<>();

    @BindView(R2.id.dialogsList) protected DialogsList dialogsList;
    @BindView(R2.id.root) protected RelativeLayout root;

    protected boolean listenersAdded = false;
    protected boolean didLoadData = false;

//    protected UpdateActionBatcher batcher = new UpdateActionBatcher(100);

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_threads;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
//        reloadData();
//        addListeners();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();
        hideKeyboard();

        return view;
    }

    public void addListeners() {

        if (listenersAdded) {
            return;
        }
        listenersAdded = true;

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(
                EventType.ThreadsUpdated,
                EventType.ThreadAdded,
                EventType.ThreadRemoved,
                EventType.MessageAdded,
                EventType.MessageRemoved,
                EventType.TypingStateUpdated
        )).subscribe(networkEvent -> {

            if (networkEvent.typeIs(EventType.ThreadsUpdated)) {
                loadData();
            } else {
                final Thread thread = networkEvent.getThread();
                if (thread != null) {
                    ThreadHolder holder = threadHolderHashMap.get(thread);
                    final boolean inList = holder != null;

                    if (networkEvent.typeIs(EventType.ThreadAdded)) {
                        if (!inList) {
                            addThread(thread, true, true);
//                            addOrUpdateThread(thread);
                        }
                    }
                    else if (networkEvent.typeIs(EventType.ThreadRemoved)) {
                        if (inList) {
                            removeThread(thread);
                        }
                    }
                    // TODO: Thread
                    else if (networkEvent.typeIs(EventType.MessageAdded, EventType.MessageRemoved)) {
                        if (inList) {
                            updateThread(thread);
//                            sortByLastMessageDate();
                        }
                    }
//                    else {
//                        if (inList) {
//                            dialogsListAdapter.updateItemById(holder);
//                        }
//                    }
                }

            }
        }));

        dm.add(ChatSDK.events().sourceOnBackground()
//                .filter(mainEventFilter())
                .filter(NetworkEvent.filterType(EventType.Logout))
                .observeOn(RX.main())
                .subscribe(networkEvent -> {

//                    if (networkEvent.typeIs(EventType.Logout)) {
                        threadHolderHashMap.clear();
                        dialogsListAdapter.clear();
//                    } else {
//                        batcher.add(networkEvent, networkEvent.typeIs(EventType.TypingStateUpdated));
//                    }
//
//                    Logger.debug("Network Event: " + networkEvent.type);


                }));
    }

    public void removeListeners() {
        listenersAdded = false;
//        if(batcher != null) {
//            batcher.dispose();
//            batcher.dispose();
//        }
        dm.dispose();
    }

    public boolean inList(Thread thread) {
        return thread != null && dialogsListAdapter.getItemById(thread.getEntityID()) != null;
//        return threadHolderHashMap.containsKey(thread);
    }

    public void initViews() {

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.view_holder_thread, ThreadViewHolder.class, (imageView, url, payload) -> {
            if (getContext() != null) {
                int size = Dimen.from(getContext(), R.dimen.action_bar_avatar_size);

                if (payload instanceof ThreadHolder) {
                    ThreadHolder threadHolder = (ThreadHolder) payload;
                    ThreadImageBuilder.load(imageView, threadHolder.getThread());
                } else {
                    int placeholder = UIModule.config().defaultProfilePlaceholder;
                    GlideWith.load(this, url).dontAnimate().override(size).placeholder(placeholder).into(imageView);
                }
            }
        });

        if (UIModule.config().threadTimeFormat != null) {
            dialogsListAdapter.setDatesFormatter(date -> {
                return DateFormatter.format(date, UIModule.config().threadTimeFormat);
            });
        }

        dialogsList.setAdapter(dialogsListAdapter);

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

        dialogsListAdapter.setOnDialogViewClickListener((view, dialog) -> {
            dialog.markRead();
            startChatActivity(dialog.getId());
        });
        dialogsListAdapter.setOnDialogLongClickListener(dialog -> {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(dialog.getId());
            if (thread != null) {
                onLongClickPublishRelay.accept(thread);
            }
        });
    }

    protected void startChatActivity(String threadEntityID) {
        ChatSDK.ui().startChatActivityForID(getContext(), threadEntityID);
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
        loadData();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void clearData() {
        if (dialogsListAdapter != null) {
            dialogsListAdapter.clear();
        }
        threadHolderHashMap.clear();
    }

    public void setTabVisibility(boolean isVisible) {
        super.setTabVisibility(isVisible);
        if (isVisible) {
            softReloadData();
        }
    }

    @Override
    public void reloadData() {
        // TODO: Thread
        loadData();
    }

    public void softReloadData() {
        if (dialogsListAdapter != null) {
            dialogsListAdapter.notifyDataSetChanged();
        }
    }

    public void loadData() {
        if (dialogsListAdapter != null) {
            if (!didLoadData) {
                getThreads().map(threads -> {
                    threads = filter(threads);

                    threadHolders.clear();
                    for (Thread thread : threads) {
                        addThread(thread, false, false);
                    }
                    sortThreadHolders();
                    return threadHolders;
                }).observeOn(RX.single()).observeOn(RX.main()).doOnSuccess(threadHolders -> {
                    synchronize();
                    addListeners();
                }).subscribe();

                didLoadData = true;
            } else {
                sortThreadHolders();
                synchronize();
            }
        }
    }

//    public ThreadHolder getOrCreateThreadHolder(Thread thread) {
//        ThreadHolder holder = threadHolderHashMap.get(thread);
//        if (holder == null) {
//            holder = createThreadHolder(thread);
//        }
//        return holder;
//    }

    public void addThread(Thread thread, boolean sort, boolean sync) {
        if (!threadHolderExists(thread)) {
            ThreadHolder holder = createThreadHolder(thread);
            threadHolders.add(holder);
            if (sort) {
                sortThreadHolders();
            }
            if (sync) {
                synchronize();
            }
        }
    }

    // Synchronize the thread holders with the list
    protected void synchronize() {

        long start = System.currentTimeMillis();

        List<ThreadHolder> newHolders = new ArrayList<>(threadHolders);
        List<ThreadHolder> oldHolders = new ArrayList<>(dialogsListAdapter.getItems());
        ThreadHoldersDiffCallback callback = new ThreadHoldersDiffCallback(newHolders, oldHolders);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);

        dialogsListAdapter.getItems().clear();
        dialogsListAdapter.setItems(newHolders);

        result.dispatchUpdatesTo(dialogsListAdapter);

        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println("Diff: " + diff);

    }

    protected class UpdateThread {
        int from = -1;
        int to = -1;
        boolean doUpdate() {
            return from != -1 && to != -1 && from != to;
        }
    }

    public void updateThread(final Thread thread) {
        final ThreadHolder holder = threadHolderHashMap.get(thread);
        if (holder != null) {

            holder.update();
            synchronize();

            sortThreadHolders();

            int from = dialogsListAdapter.getItems().indexOf(holder);
            int to = threadHolders.indexOf(holder);

            // TODO: Thread
//            dialogsListAdapter.updateItemById(holder);
            if (from >=0) {
                if (to >= 0 && from != to) {
                    dialogsListAdapter.moveItem(from, to);
                } else {
                    // TODO: Doesn't seem to cause update
                    dialogsListAdapter.notifyItemChanged(from);
                }
            }
        }

//        if (holder != null) {
//            Single.create((SingleOnSubscribe<UpdateThread>) emitter -> {
//
//                sortThreadHolders();
//
//                UpdateThread update = new UpdateThread();
//
//                update.from = dialogsListAdapter.getItems().indexOf(holder);
//                update.to = threadHolders.indexOf(holder);
//
//            }).subscribeOn(RX.single()).observeOn(RX.main()).doOnSuccess(update -> {
//
//                dialogsListAdapter.updateItemById(holder);
//                if (update.doUpdate()) {
//                    dialogsListAdapter.moveItem(update.from, update.to);
//                }
//
//            }).subscribe();

    }

    public void sortThreadHolders() {
        Collections.sort(threadHolders, (o1, o2) -> {
            if (!o1.getWeight().equals(o2.getWeight())) {
                return o1.getWeight().compareTo(o2.getWeight());
            }
            return o2.getDate().compareTo(o1.getDate());
        });
    }

//    public void sortByLastMessageDate() {
////         TODO: Thread
//        dialogsListAdapter.sort((o1, o2) -> {
//            if (!o1.getWeight().equals(o2.getWeight())) {
//                return o1.getWeight().compareTo(o2.getWeight());
//            }
//            return o2.getDate().compareTo(o1.getDate());
//        });
//    }

//    public Single<ThreadHolder> getOrCreateThreadHolderAsync(Thread thread) {
//        return Single.create((SingleOnSubscribe<ThreadHolder>) emitter -> {
//            ThreadHolder holder = threadHolderHashMap.get(thread);
//            if (holder == null) {
//                holder = new ThreadHolder(thread);
//                threadHolderHashMap.put(thread, holder);
//            }
//            emitter.onSuccess(holder);
//        }).subscribeOn(RX.single());
//    }
//

    public ThreadHolder createThreadHolder(Thread thread) {
        ThreadHolder holder = new ThreadHolder(thread);
        threadHolderHashMap.put(thread, holder);
        return holder;
    }

    public boolean threadHolderExists(Thread thread) {
        return threadHolderHashMap.containsKey(thread);
    }

    public void removeThread(Thread thread) {
        ThreadHolder holder = threadHolderHashMap.get(thread);
        if (holder != null) {
            threadHolderHashMap.remove(thread);
            threadHolders.remove(holder);
            synchronize();
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
