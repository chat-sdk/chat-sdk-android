/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.SearchActivityType;
import co.chatsdk.core.utils.UserListItemConverter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.adapters.UsersListAdapter;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.interfaces.SearchSupported;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends BaseFragment implements SearchSupported {

    protected UsersListAdapter adapter;

    protected PublishSubject<User> onClickSubject = PublishSubject.create();
    protected PublishSubject<User> onLongClickSubject = PublishSubject.create();
    protected Disposable listOnClickListenerDisposable;
    protected Disposable listOnLongClickListenerDisposable;

    protected String filter;

    protected List<User> sourceUsers = new ArrayList<>();

    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;
    @BindView(R2.id.root) protected FrameLayout root;


    protected @LayoutRes
    int getLayout() {
        return R.layout.fragment_contacts;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();
        addListeners();

        loadData(true);

        return view;
    }

    public void addListeners() {

        if (listOnClickListenerDisposable != null) {
            listOnClickListenerDisposable.dispose();
        }
        listOnClickListenerDisposable = adapter.onClickObservable().subscribe(o -> {
            if (o instanceof User) {
                final User clickedUser = (User) o;

                onClickSubject.onNext(clickedUser);
                startProfileActivity(clickedUser.getEntityID());
            }
        });

        if (listOnLongClickListenerDisposable != null) {
            listOnLongClickListenerDisposable.dispose();
        }
        listOnLongClickListenerDisposable = adapter.onLongClickObservable().subscribe(o -> {
            if (o instanceof User) {
                onLongClickSubject.onNext((User) o);
            }
        });

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterContactsChanged())
                .subscribe(networkEvent -> loadData(true)));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> loadData(true)));
    }

    public void startProfileActivity(String userEntityID) {

//        Pair<View, String> p1 = Pair.create(view.findViewById(R.id.dialogAvatar), "imageView");
//        Pair<View, String> p2 = Pair.create(view.findViewById(R.id.dialogName), "titleTextView");
//
//        Bundle bundle = null;
//        Activity activity = getActivity();
//        if (activity != null) {
//            bundle = ActivityOptionsCompat.
//                    makeSceneTransitionAnimation(activity, p1, p2).toBundle();
//        }

        ChatSDK.ui().startProfileActivity(getContext(), userEntityID);

    }

    public void initViews() {

        // Create the adapter only if null this is here so we wont
        // override the adapter given from the extended class with setAdapter.
        adapter = new UsersListAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_menu, menu);
        menu.findItem(R.id.action_add).setIcon(Icons.get(Icons.choose().add, R.color.app_bar_icon_color));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        // Each user that will be found in the filter context will be automatically added as a contact.
        if (id == R.id.action_add) {
            startSearchActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startSearchActivity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final List<SearchActivityType> activities = new ArrayList<>(ChatSDK.ui().getSearchActivities());
        activities.add(new SearchActivityType(ChatSDK.ui().getSearchActivity(), getActivity().getString(R.string.search_with_name)));

        if (activities.size() == 1) {
            ChatSDK.ui().startActivity(getActivity(), activities.get(0).className);
            return;
        }

        String[] items = new String[activities.size()];
        int i = 0;

        for (SearchActivityType activity : activities) {
            items[i++] = activity.title;
        }

        builder.setTitle(getActivity().getString(R.string.search)).setItems(items, (dialogInterface, index) -> {
            // Launch the appropriate context
            ChatSDK.ui().startActivity(getActivity(), activities.get(index).className);
        });

        builder.show();
    }

    public void loadData(final boolean force) {
        final ArrayList<User> originalUserList = new ArrayList<>(sourceUsers);

        reloadData();

        if (!originalUserList.equals(sourceUsers) || force) {
            adapter.setUsers(UserListItemConverter.toUserItemList(sourceUsers), true);
        }
    }

    @Override
    public void clearData() {
        if (adapter != null) {
            adapter.clear();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData(true);
    }

    @Override
    public void reloadData() {
        sourceUsers.clear();
        sourceUsers.addAll(filter(ChatSDK.contact().contacts()));
    }

    public Observable<User> onClickObservable() {
        return onClickSubject;
    }

    public Observable<User> onLongClickObservable() {
        return onLongClickSubject;
    }

    @Override
    public void filter(String text) {
        filter = text;
        loadData(false);
    }

    public List<User> filter(List<User> users) {
        if (filter == null || filter.isEmpty()) {
            return users;
        }

        List<User> filteredUsers = new ArrayList<>();
        for (User u : users) {
            if (u.getName() != null && u.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredUsers.add(u);
            }
        }
        return filteredUsers;
    }

}
