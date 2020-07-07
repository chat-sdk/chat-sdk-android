package sdk.chat.ui.fragments;

import android.graphics.drawable.Drawable;
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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.jakewharton.rxrelay2.PublishRelay;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Predicate;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.chat.model.TypingThreadHolder;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.interfaces.SearchSupported;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.update.ThreadUpdateAction;
import sdk.chat.ui.update.UpdateActionBatcher;
import sdk.chat.ui.utils.ThreadImageBuilder;
import sdk.chat.ui.view_holders.ThreadViewHolder;
import sdk.guru.common.RX;

public abstract class ThreadsFragment extends BaseFragment implements SearchSupported {

    protected String filter;

    protected DialogsListAdapter<ThreadHolder> dialogsListAdapter;
    protected HashMap<Thread, ThreadHolder> threadHolderHashMap = new HashMap<>();

    protected PublishRelay<Thread> onClickPublishRelay = PublishRelay.create();
    protected PublishRelay<Thread> onLongClickPublishRelay = PublishRelay.create();

    @BindView(R2.id.dialogsList) protected DialogsList dialogsList;
    @BindView(R2.id.root) protected RelativeLayout root;

    protected UpdateActionBatcher batcher = new UpdateActionBatcher(100);
    ;

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

        initViews();
//        addListeners();

        loadData();

        hideKeyboard();

        return view;
    }

    public void addListeners() {

        removeListeners();

        this.batcher = new UpdateActionBatcher(100);

        // We batch updates to the threads fragment because potentially it could be updated from a lot of places
        // Thread meta, user meta, user presence, messages, read receipts
        // So we batch and combine updates to make it more efficient
        dm.add(batcher.onUpdate().observeOn(RX.main()).subscribe(threadUpdateActions -> {
            for (ThreadUpdateAction action : threadUpdateActions) {
                if (action.type == ThreadUpdateAction.Type.Reload) {
                    reloadData();
                } else {
                    if (action.type == ThreadUpdateAction.Type.SoftReload) {
                        softReloadData();
                    }
                    if (action.type == ThreadUpdateAction.Type.Add) {
                        addOrUpdateThread(action.thread);
                    } else if (action.type == ThreadUpdateAction.Type.Remove) {
                        removeThread(action.thread);
                    } else if (action.type == ThreadUpdateAction.Type.Update) {
                        dialogsListAdapter.updateItemById(action.holder);
                    } else if (action.type == ThreadUpdateAction.Type.UpdateMessage) {
                        updateMessage(action.message);
                    }
                }
            }
        }, throwable -> {
            onError(throwable);
        }));

        dm.add(ChatSDK.events().sourceOnBackground()
                .filter(mainEventFilter())
                .subscribe(networkEvent -> {

                    Logger.debug("Network Event: " + networkEvent.type);

                    final Thread thread = networkEvent.getThread();
                    final boolean inList = inList(thread);

                    // This stops a case where the thread details updated could be called before the thread is added
                    if (networkEvent.typeIs(EventType.ThreadAdded)) {
                        if (!inList) {
                            batcher.addAction(ThreadUpdateAction.add(thread));
                        }
                    } else if (networkEvent.typeIs(EventType.ThreadRemoved)) {
                        if (inList) {
                            batcher.addAction(ThreadUpdateAction.remove(thread));
                        }
                    }
                    else if (networkEvent.typeIs(EventType.ThreadDetailsUpdated, EventType.ThreadUsersUpdated, EventType.MessageReadReceiptUpdated)) {
                        if (inList) {
                            batcher.addAction(ThreadUpdateAction.add(thread));
                        }
                    }
                    else if (networkEvent.typeIs(EventType.TypingStateUpdated)) {
                        if (inList) {
                            if (networkEvent.getText() != null) {
                                String typingText = networkEvent.getText();
                                typingText += getString(R.string.typing);
                                batcher.addAction(ThreadUpdateAction.update(thread, new TypingThreadHolder(thread, typingText)));
                            } else {
                                getOrCreateThreadHolderAsync(thread).observeOn(RX.main()).doOnSuccess(holder -> {
                                    batcher.addAction(ThreadUpdateAction.update(thread, holder));
                                }).subscribe();
                            }
                        }
                    }
                    else if (networkEvent.typeIs(EventType.UserMetaUpdated, EventType.UserPresenceUpdated)) {
                        batcher.addSoftReload();
                    } else if (networkEvent.typeIs(EventType.MessageAdded, EventType.MessageRemoved, EventType.MessageReadReceiptUpdated)) {
                        final Message message = networkEvent.getMessage();
                        if (message != null) {
                            Message lastMessage = thread.lastMessage();
                            if (lastMessage == null || lastMessage.equals(message)) {
                                batcher.addAction(ThreadUpdateAction.updateMessage(message));
                            }
                        }
                    } else {
                        batcher.addReload();
                    }
                }));
    }

    public void removeListeners() {
        if(batcher != null) {
            batcher.dispose();
        }
        dm.dispose();
    }

    public boolean inList(Thread thread) {
        return dialogsListAdapter.getItemById(thread.getEntityID()) != null;
//        return threadHolderHashMap.containsKey(thread);
    }

    public void initViews() {

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.view_holder_thread, ThreadViewHolder.class, (imageView, url, payload) -> {
            if (getContext() != null) {
                int size = Dimen.from(getContext(), R.dimen.action_bar_avatar_size);

                if (payload instanceof ThreadHolder) {
                    if (url == null) {
                        ThreadHolder threadHolder = (ThreadHolder) payload;
                        dm.add(ThreadImageBuilder.load(imageView, threadHolder.getThread(), size));
                    } else {
                        Drawable placeholder = ThreadImageBuilder.defaultDrawable(null);
                        Glide.with(this).load(url).dontAnimate().override(size).placeholder(placeholder).into(imageView);
                    }
                } else {
                    int placeholder = UIModule.config().defaultProfilePlaceholder;
                    Glide.with(this).load(url).dontAnimate().override(size).placeholder(placeholder).into(imageView);
                }
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);

//                Sometimes a new group is not registered
//                Create new thread not ordered properly
//                when a new message is received on a group, it has no avatar (fixed)


        // Stop the image from flashing when the list is reloaded
        RecyclerView.ItemAnimator animator = dialogsList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

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
        inflater.inflate(R.menu.add_menu, menu);
        menu.findItem(R.id.action_add).setIcon(Icons.get(getContext(), Icons.choose().add, Icons.shared().actionBarIconColor));
        if (!allowThreadCreation()) {
            menu.removeItem(R.id.action_add);
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
        reloadData();
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
    }

    public void setTabVisibility(boolean isVisible) {
        super.setTabVisibility(isVisible);
        if (isVisible) {
            softReloadData();
        }
    }

    @Override
    public void reloadData() {
        loadData();
    }

    public void softReloadData() {
        if (dialogsListAdapter != null) {
            dialogsListAdapter.notifyDataSetChanged();
        }
    }

    public void loadData() {
        if (dialogsListAdapter != null) {
            getThreads().observeOn(RX.single()).map(threads -> {
                ArrayList<ThreadHolder> threadHolders = new ArrayList<>();
                threads = filter(threads);
                for (Thread thread : threads) {
                    ThreadHolder holder = getOrCreateThreadHolder(thread);
                    holder.update();
                    threadHolders.add(holder);
                }
                return threadHolders;
            }).observeOn(RX.main()).doOnSuccess(threadHolders -> {
                dialogsListAdapter.clear();
                dialogsListAdapter.setItems(threadHolders);
                if (threadHolders.size() > 1) {
                    sortByLastMessageDate();
                }
            }).subscribe();
        }
    }

    protected void reloadThread(Thread thread) {
        getOrCreateThreadHolderAsync(thread).observeOn(RX.main()).doOnSuccess(holder -> {
            dialogsListAdapter.updateItemById(holder);
        }).subscribe();
    }

    protected void updateMessage(Message message) {
        ThreadHolder holder = threadHolderHashMap.get(message.getThread());
        if (holder != null) {
            holder.updateAsync().observeOn(RX.main()).doOnComplete(() -> {
                dialogsListAdapter.updateItemById(holder);
            }).subscribe(this);
        } else {
            addOrUpdateThread(message.getThread());
        }
    }

    public void addOrUpdateThread(Thread thread) {
        ThreadHolder holder = threadHolderHashMap.get(thread);
        if (holder == null) {
            getOrCreateThreadHolderAsync(thread).observeOn(RX.main()).doOnSuccess(holder1 -> {
                if (dialogsListAdapter.getItemById(holder1.getId()) == null) {
                    dialogsListAdapter.addItem(holder1);
                }
                sortByLastMessageDate();
            }).ignoreElement().subscribe(this);
        } else {
            dialogsListAdapter.updateItemById(holder);
            sortByLastMessageDate();
        }
    }

    public void sortByLastMessageDate() {
        dialogsListAdapter.sort((o1, o2) -> {
            if (!o1.getWeight().equals(o2.getWeight())) {
                return o1.getWeight().compareTo(o2.getWeight());
            }
            return o2.getDate().compareTo(o1.getDate());
        });
    }

    public Single<ThreadHolder> getOrCreateThreadHolderAsync(Thread thread) {
        return Single.create((SingleOnSubscribe<ThreadHolder>) emitter -> {
            ThreadHolder holder = threadHolderHashMap.get(thread);
            if (holder == null) {
                holder = new ThreadHolder(thread);
                threadHolderHashMap.put(thread, holder);
            }
            emitter.onSuccess(holder);
        }).subscribeOn(RX.single());
    }

    public ThreadHolder getOrCreateThreadHolder(Thread thread) {
        ThreadHolder holder = threadHolderHashMap.get(thread);
        if (holder == null) {
            holder = new ThreadHolder(thread);
            threadHolderHashMap.put(thread, holder);
        }
        return holder;
    }

    public void removeThread(Thread thread) {
        threadHolderHashMap.remove(thread);
        dialogsListAdapter.deleteById(thread.getEntityID());
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
