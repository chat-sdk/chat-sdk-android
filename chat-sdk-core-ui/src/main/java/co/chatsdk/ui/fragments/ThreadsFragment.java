package co.chatsdk.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import co.chatsdk.ui.chat.model.MessageHolder;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.model.ThreadHolder;
import co.chatsdk.ui.chat.model.TypingThreadHolder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.SearchSupported;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.ui.utils.ThreadImageBuilder;
import co.chatsdk.ui.view_holders.ThreadViewHolder;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;
import sdk.guru.common.RX;

public abstract class ThreadsFragment extends BaseFragment implements SearchSupported {

    protected String filter;

    protected DialogsListAdapter<ThreadHolder> dialogsListAdapter;
    protected HashMap<Thread, ThreadHolder> threadHolderHashMap = new HashMap<>();

    protected PublishSubject<Thread> onClickPublishSubject = PublishSubject.create();
    protected PublishSubject<Thread> onLongClickPublishSubject = PublishSubject.create();

    @BindView(R2.id.dialogsList) protected DialogsList dialogsList;
    @BindView(R2.id.root) protected RelativeLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_threads;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();
        addListeners();

        loadData();

        hideKeyboard();

        return view;
    }

    public void addListeners() {
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(mainEventFilter())
                .subscribe(networkEvent -> {
                    Logger.debug(networkEvent.type);
                    if (networkEvent.typeIs(EventType.ThreadAdded, EventType.ThreadDetailsUpdated, EventType.ThreadUsersUpdated, EventType.MessageReadReceiptUpdated)) {
                        if (networkEvent.getThread() != null) {
                            addOrUpdateThread(networkEvent.getThread());
                        } else {
                            Logger.debug("Stop");
                        }
                    }
                    else if (networkEvent.typeIs(EventType.TypingStateUpdated)) {
                        if (networkEvent.getText() != null) {
                            String typingText = networkEvent.getText();
                            typingText += getString(R.string.typing);
                            dialogsListAdapter.updateItemById(new TypingThreadHolder(networkEvent.getThread(), typingText));
                        } else {
                            getOrCreateThreadHolderAsync(networkEvent.getThread()).observeOn(RX.main()).doOnSuccess(holder -> {
                                dialogsListAdapter.updateItemById(holder);
                            }).subscribe();
                        }
                    }
                    else if (networkEvent.typeIs(EventType.UserMetaUpdated, EventType.UserPresenceUpdated)) {
                        softReloadData();
                    } else if (networkEvent.typeIs(EventType.ThreadRemoved)) {
                        removeThread(networkEvent.getThread());
                    } else if (networkEvent.typeIs(EventType.MessageAdded, EventType.MessageRemoved, EventType.MessageReadReceiptUpdated)) {
                        if (networkEvent.getMessage() != null) {
                            updateMessage(networkEvent.getMessage(), networkEvent.typeIs(EventType.MessageRemoved));
                        }
                    } else {
                        reloadData();
                    }
                }));
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
                    int placeholder = DefaultUIModule.config().defaultProfileImage;
                    Glide.with(this).load(url).dontAnimate().override(size).placeholder(placeholder).into(imageView);
                }
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);
        
                Sometimes a new group is not registered
                Create new thread not ordered properly
                when a new message is received on a group, it has no avatar


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
                onLongClickPublishSubject.onNext(thread);
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
        menu.findItem(R.id.action_add).setIcon(Icons.get(Icons.choose().add, Icons.shared().actionBarIconColor));
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
//        softReloadData();
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
            getThreads().observeOn(RX.db()).map(threads -> {
                ArrayList<ThreadHolder> threadHolders = new ArrayList<>();
                threads = filter(threads);
                for (Thread thread : threads) {
                    ThreadHolder holder = getOrCreateThreadHolder(thread);
                    threadHolders.add(holder);
                }
                return threadHolders;
            }).observeOn(RX.main()).doOnSuccess(threadHolders -> {
                dialogsListAdapter.clear();
                dialogsListAdapter.setItems(threadHolders);
                if (threadHolders.size() > 1) {
                    dialogsListAdapter.sortByLastMessageDate();
                }
            }).subscribe();
        }
    }

    protected void reloadThread(Thread thread) {
        getOrCreateThreadHolderAsync(thread).observeOn(RX.main()).doOnSuccess(holder -> {
            dialogsListAdapter.updateItemById(holder);
        }).subscribe();
    }

    protected void updateMessage(Message message, boolean didRemove) {
        ThreadHolder holder = threadHolderHashMap.get(message.getThread());
        if (holder != null) {
            if (didRemove) {
                dialogsListAdapter.updateItemById(holder);
            } else {
                MessageHolder messageHolder = Customiser.shared().onNewMessageHolder(message);
                holder.setLastMessage(messageHolder);
                dialogsListAdapter.updateDialogWithMessage(message.getThread().getEntityID(), messageHolder);
            }
        } else {
            getOrCreateThreadHolderAsync(message.getThread()).observeOn(RX.main()).doOnSuccess(threadHolder -> {
                dialogsListAdapter.addItem(threadHolder);
            }).subscribe();
        }
    }

    public void addOrUpdateThread(Thread thread) {
        ThreadHolder holder = threadHolderHashMap.get(thread);
        if (holder == null) {
            holder = new ThreadHolder(thread);
            threadHolderHashMap.put(thread, holder);
            dialogsListAdapter.addItem(new ThreadHolder(thread));
        } else {
            dialogsListAdapter.updateItemById(holder);
        }
        dialogsListAdapter.sortByLastMessageDate();
    }

    public Single<ThreadHolder> getOrCreateThreadHolderAsync(Thread thread) {
        return Single.create((SingleOnSubscribe<ThreadHolder>) emitter -> {
            ThreadHolder holder = threadHolderHashMap.get(thread);
            if (holder == null) {
                holder = new ThreadHolder(thread);
                threadHolderHashMap.put(thread, holder);
            }
            emitter.onSuccess(holder);
        }).subscribeOn(RX.db());
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
        return onLongClickPublishSubject.hide();
    }

    public void filter(String text) {
        filter = text;
        loadData();
    }

}
