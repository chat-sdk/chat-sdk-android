package co.chatsdk.ui.chatkit;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.LayoutRes;

import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseFragment;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import co.chatsdk.ui.chatkit.custom.DialogViewHolder;
import co.chatsdk.ui.chatkit.model.MessageHolder;
import co.chatsdk.ui.chatkit.model.ThreadHolder;
import co.chatsdk.ui.chatkit.model.TypingThreadHolder;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

public abstract class CKThreadsFragment extends BaseFragment {

    protected EditText searchField;
    protected String filter;
    protected MenuItem addMenuItem;

    protected DialogsList dialogsList;
    protected DialogsListAdapter<ThreadHolder> dialogsListAdapter;

    protected PublishSubject<Thread> onClickPublishSubject = PublishSubject.create();
    protected PublishSubject<Thread> onLongClickPublishSubject = PublishSubject.create();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(activityLayout(), null);

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(mainEventFilter())
                .subscribe(networkEvent -> {
                    if (tabIsVisible) {
                        if (networkEvent.typeIs(EventType.ThreadAdded)) {
                            addThread(networkEvent.thread);
                        }
//                        else if (networkEvent.typeIs(EventType.ThreadRemoved)) {
//                            reloadData();
//                        }
                        else if (networkEvent.typeIs(EventType.ThreadDetailsUpdated, EventType.ThreadUsersChanged, EventType.UserMetaUpdated)) {
                            updateThread(networkEvent.thread);
                        }
                        else if (networkEvent.typeIs(EventType.ThreadLastMessageUpdated)) {
                            updateMessage(networkEvent.message);
                        } else {
                            reloadData();
                        }
                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .subscribe(networkEvent -> {
                    if (tabIsVisible) {
                        if (networkEvent.text != null && !networkEvent.text.isEmpty()) {
                            dialogsListAdapter.updateItemById(new TypingThreadHolder(networkEvent.thread, networkEvent.text));
                        } else {
                            dialogsListAdapter.updateItemById(new ThreadHolder(networkEvent.thread));
                        }
                    }
                }));


        initViews();

        loadData();

        return mainView;
    }

    protected abstract Predicate<NetworkEvent> mainEventFilter ();

    protected  @LayoutRes
    int activityLayout () {
        return R.layout.fragment_chatkit_threads;
    }

    public void initViews() {
        searchField = mainView.findViewById(R.id.search_field);

        dialogsList = mainView.findViewById(R.id.dialogsList);

        dialogsListAdapter = new DialogsListAdapter<>(R.layout.chatkit_dialog_view_holder, DialogViewHolder.class, (imageView, url, payload) -> {
            if (getContext() != null) {
                if (url != null) {
                    Picasso.get().load(url).placeholder(ThreadImageBuilder.defaultBitmapResId()).into(imageView);
                } else if (payload instanceof ThreadHolder) {
                    ThreadHolder threadHolder = (ThreadHolder) payload;
                    int size = getContext().getResources().getDimensionPixelSize(R.dimen.action_bar_avatar_max_size);
                    dm.add(ThreadImageBuilder.load(imageView, threadHolder.getThread(), size));
                } else {
                    imageView.setImageResource(ThreadImageBuilder.defaultBitmapResId());
                }
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);

        dialogsListAdapter.setOnDialogClickListener(dialog -> {
            ChatSDK.ui().startChatActivityForID(getContext(), dialog.getId());
        });
        dialogsListAdapter.setOnDialogLongClickListener(dialog -> {
            Thread thread = ChatSDK.db().fetchThreadWithEntityID(dialog.getId());
            if (thread != null) {
                onLongClickPublishSubject.onNext(thread);
            }
        });
    }

    protected boolean allowThreadCreation () {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (allowThreadCreation()) {
            addMenuItem = menu.add(Menu.NONE, R.id.action_add, 10, getString(R.string.thread_fragment_add_item_text));
            addMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            addMenuItem.setIcon(R.drawable.ic_plus);
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

        if (searchField != null) {
            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter = searchField.getText().toString();
                    loadData();
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    @Override
    public void clearData() {
        if (dialogsListAdapter != null) {
            dialogsListAdapter.clear();
        }
    }

    public void setTabVisibility (boolean isVisible) {
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
            for (Thread thread: threads) {
                threadHolders.add(new ThreadHolder(thread));
            }
            dialogsListAdapter.setItems(threadHolders);
        }
    }

    protected void reloadThread(Thread thread) {
        dialogsListAdapter.updateItemById(new ThreadHolder(thread));
    }

    protected void updateMessage(Message message) {
        if(!dialogsListAdapter.updateDialogWithMessage(message.getThread().getEntityID(), new MessageHolder(message))) {
            dialogsListAdapter.addItem(new ThreadHolder(message.getThread()));
        }
    }

    public void addThread(Thread thread) {
        dialogsListAdapter.addItem(new ThreadHolder(thread));
    }

    public void updateThread(Thread thread) {
        dialogsListAdapter.updateItemById(new ThreadHolder(thread));
    }

    protected abstract List<Thread> getThreads ();

    public List<Thread> filter (List<Thread> threads) {
        if (filter == null || filter.isEmpty()) {
            return threads;
        }

        List<Thread> filteredThreads = new ArrayList<>();
        for (Thread t : threads) {
            if (t.getName() != null && t.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredThreads.add(t);
            }
            else {
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
}
