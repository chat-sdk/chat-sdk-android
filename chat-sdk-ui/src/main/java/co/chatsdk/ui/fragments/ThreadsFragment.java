package co.chatsdk.ui.fragments;

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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.chat.model.ThreadHolder;
import co.chatsdk.ui.chat.model.TypingThreadHolder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.SearchSupported;
import co.chatsdk.ui.utils.ThreadImageBuilder;
import co.chatsdk.ui.view_holders.ThreadViewHolder;
import io.reactivex.Observable;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

public abstract class ThreadsFragment extends BaseFragment implements SearchSupported {

    protected String filter;

    protected DialogsListAdapter<ThreadHolder> dialogsListAdapter;
    protected HashMap<Thread, ThreadHolder> threadHolderHashMap = new HashMap<>();

    protected PublishSubject<Thread> onClickPublishSubject = PublishSubject.create();
    protected PublishSubject<Thread> onLongClickPublishSubject = PublishSubject.create();

    @BindView(R2.id.dialogsList) protected DialogsList dialogsList;
    @BindView(R2.id.root) protected RelativeLayout root;

    protected @LayoutRes
    int getLayout() {
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
//                    if (tabIsVisible) {
                        if (networkEvent.typeIs(EventType.ThreadAdded, EventType.ThreadDetailsUpdated, EventType.ThreadUsersUpdated, EventType.UserMetaUpdated, EventType.UserPresenceUpdated)) {
                            addOrUpdateThread(networkEvent.thread);
                        } else if (networkEvent.typeIs(EventType.ThreadRemoved)) {
                            removeThread(networkEvent.thread);
                        } else if (networkEvent.typeIs(EventType.MessageAdded)) {
                            if (networkEvent.message != null) {
                                updateMessage(networkEvent.message);
                            }
                        } else {
                            reloadData();
                        }
//                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateUpdated))
                .subscribe(networkEvent -> {
                    if (networkEvent.text != null) {
                        String typingText = networkEvent.text;
                        typingText += getString(R.string.typing);
                        dialogsListAdapter.updateItemById(new TypingThreadHolder(networkEvent.thread, typingText));
                    } else {
                        ThreadHolder holder = getOrCreateThreadHolder(networkEvent.thread);
                        dialogsListAdapter.updateItemById(holder);
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
                    int placeholder = ChatSDK.ui().getDefaultProfileImage();
                    Glide.with(this).load(url).dontAnimate().override(size).placeholder(placeholder).into(imageView);
                }
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);

        // Stop the image from flashing when the list is reloaded
        RecyclerView.ItemAnimator animator = dialogsList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        dialogsListAdapter.setOnDialogViewClickListener((view, dialog) -> startChatActivity(dialog.getId()));
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
        menu.findItem(R.id.action_add).setIcon(Icons.get(Icons.choose().add, R.color.app_bar_icon_color));
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
        softReloadData();
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
            dialogsListAdapter.clear();
            ArrayList<ThreadHolder> threadHolders = new ArrayList<>();
            List<Thread> threads = filter(getThreads());
            for (Thread thread : threads) {
                ThreadHolder holder = getOrCreateThreadHolder(thread);
                threadHolders.add(holder);
            }
            dialogsListAdapter.setItems(threadHolders);
        }
    }

    protected void reloadThread(Thread thread) {
        ThreadHolder holder = getOrCreateThreadHolder(thread);
        dialogsListAdapter.updateItemById(holder);
    }

    protected void updateMessage(Message message) {
        ThreadHolder holder = threadHolderHashMap.get(message.getThread());
        if (holder != null) {
            holder.setLastMessage(null);
            dialogsListAdapter.updateDialogWithMessage(message.getThread().getEntityID(), Customiser.shared().onNewMessageHolder(message));
        } else {
            holder = getOrCreateThreadHolder(message.getThread());
            dialogsListAdapter.addItem(holder);
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

    protected abstract List<Thread> getThreads();

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
