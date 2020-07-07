/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.fragments;

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

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.types.SearchActivityType;
import sdk.chat.core.utils.UserListItemConverter;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.interfaces.SearchSupported;
import sdk.chat.ui.utils.DialogUtils;
import sdk.guru.common.Optional;
import sdk.guru.common.RX;

/**
 * Created by itzik on 6/17/2014.
 */
public class ContactsFragment extends BaseFragment implements SearchSupported {

    protected UsersListAdapter adapter;

    protected PublishRelay<User> onClickRelay = PublishRelay.create();
    protected PublishRelay<User> onLongClickRelay = PublishRelay.create();
    protected Disposable listOnClickListenerDisposable;
    protected Disposable listOnLongClickListenerDisposable;

    protected String filter;

    protected List<User> sourceUsers = new ArrayList<>();

    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;
    @BindView(R2.id.root) protected FrameLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_contacts;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        initViews();

        loadData(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        addListeners();

    }

    @Override
    public void onStop() {
        super.onStop();

        dm.dispose();
    }

    public void addListeners() {

        if (listOnClickListenerDisposable != null) {
            listOnClickListenerDisposable.dispose();
        }
        listOnClickListenerDisposable = adapter.onClickObservable().subscribe(o -> {
            if (o instanceof User) {
                final User clickedUser = (User) o;

                onClickRelay.accept(clickedUser);
                startProfileActivity(clickedUser.getEntityID());
            }
        });

        if (listOnLongClickListenerDisposable != null) {
            listOnLongClickListenerDisposable.dispose();
        }
        listOnLongClickListenerDisposable = adapter.onLongClickObservable().subscribe(o -> {
            if (o instanceof User) {
                final User user = (User) o;
                onLongClickRelay.accept(user);

                DialogUtils.showToastDialog(getContext(), R.string.delete_contact, 0, R.string.delete, R.string.cancel, () -> {
                    ChatSDK.contact()
                            .deleteContact(user, ConnectionType.Contact)
                            .subscribe(ContactsFragment.this);
                }, null);
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
        menu.findItem(R.id.action_add).setIcon(Icons.get(getContext(), Icons.choose().add, Icons.shared().actionBarIconColor));
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

        if (activities.size() == 1) {
            activities.get(0).startFrom(getActivity());
            return;
        }

        String[] items = new String[activities.size()];
        int i = 0;

        for (SearchActivityType activity : activities) {
            items[i++] = activity.title;
        }

        builder.setTitle(getActivity().getString(R.string.search)).setItems(items, (dialogInterface, index) -> {
            // Launch the appropriate context
            activities.get(index).startFrom(getActivity());
        });

        builder.show();
    }

    public void loadData(final boolean force) {
        dm.add(Single.create((SingleOnSubscribe<Optional<List<UserListItem>>>) emitter -> {
            final ArrayList<User> originalUserList = new ArrayList<>(sourceUsers);
            reloadData();
            if (!originalUserList.equals(sourceUsers) || force) {
                emitter.onSuccess(new Optional<>(UserListItemConverter.toUserItemList(sourceUsers)));
            } else {
                emitter.onSuccess(new Optional<>());
            }
        }).subscribeOn(RX.db()).observeOn(RX.main()).subscribe(listOptional -> {
            if (!listOptional.isEmpty()) {
                adapter.setUsers(listOptional.get(), true);
            }
        }));
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
        return onClickRelay;
    }

    public Observable<User> onLongClickObservable() {
        return onLongClickRelay;
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
