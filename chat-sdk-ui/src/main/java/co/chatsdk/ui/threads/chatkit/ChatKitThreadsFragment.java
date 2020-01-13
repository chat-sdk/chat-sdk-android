package co.chatsdk.ui.threads.chatkit;

import android.graphics.Bitmap;
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

import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ImageBuilder;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseFragment;
import co.chatsdk.ui.threads.ThreadImageBuilder;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public abstract class ChatKitThreadsFragment extends BaseFragment {

    protected EditText searchField;
    protected String filter;
    protected MenuItem addMenuItem;

    protected DialogsList dialogsList;
    protected DialogsListAdapter<ThreadView> dialogsListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(mainEventFilter())
                .subscribe(networkEvent -> {
                    if (tabIsVisible) {
                        reloadData();
                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateChanged))
                .subscribe(networkEvent -> {
                    if (tabIsVisible) {
                        // TODO:
//                        adapter.setTyping(networkEvent.thread, networkEvent.text);
//                        adapter.notifyDataSetChanged();
                    }
                }));


        mainView = inflater.inflate(activityLayout(), null);

        initViews();
        reloadData();

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

        dialogsListAdapter = new DialogsListAdapter<>((imageView, url, payload) -> {
            dm.add(ImageBuilder.bitmapForURL(getContext(), url).subscribe(imageView::setImageBitmap, throwable -> {
                imageView.setImageBitmap(ThreadImageBuilder.defaultBitmap(getContext()));
            }));
        });

        dialogsList.setAdapter(dialogsListAdapter);

        dialogsListAdapter.setOnDialogClickListener(dialog -> {
            ChatSDK.ui().startChatActivityForID(getContext(), dialog.getId());
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
        reloadData();

        if (searchField != null) {
            searchField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter = searchField.getText().toString();
                    reloadData();
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
        reloadData();
    }

    @Override
    public void reloadData() {
        if (dialogsListAdapter != null) {
            dialogsListAdapter.clear();
            ArrayList<ThreadView> threadViews = new ArrayList<>();
            List<Thread> threads = filter(getThreads());
            for (Thread thread: threads) {
                threadViews.add(new ThreadView(thread));
            }
            dialogsListAdapter.setItems(threadViews);
        }
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
}
