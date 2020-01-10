package co.chatsdk.ui.users;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.UserListItemConverter;
import co.chatsdk.ui.R;
import co.chatsdk.ui.contacts.UsersListAdapter;
import co.chatsdk.ui.main.BaseFragment;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class ThreadUsersFragment extends BaseFragment {

    protected UsersListAdapter adapter;
    protected ProgressBar progressBar;
    protected RecyclerView recyclerView;

    protected PublishSubject<User> onClickSubject = PublishSubject.create();
    protected PublishSubject<User> onLongClickSubject = PublishSubject.create();

    protected List<User> sourceUsers = new ArrayList<>();

    protected Thread thread;

    protected AlertDialog userDialog;
    protected AlertDialog rolesDialog;

    public class Option {

        @StringRes
        public int resId;
        public Consumer<User> action;

        public Option(int resId, Consumer<User> action) {
            this.resId = resId;
            this.action = action;
        }

        public String getText(@NonNull Activity activity) {
            return activity.getString(resId);
        }
    }

    protected ArrayList<Option> options = new ArrayList<>();

    public ThreadUsersFragment(Thread thread) {
        this.thread = thread;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (thread == null) {
            String threadEntityID = savedInstanceState.getString(Keys.IntentKeyThreadEntityID);
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        }

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterContactsChanged())
                .subscribe(networkEvent -> loadData(true)));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> loadData(true)));

        // Add the click options
        options.add(new Option(R.string.info, this::showProfile));

        if (ChatSDK.thread().rolesEnabled(thread)) {
            options.add(new Option(R.string.change_role, this::showRoleListDialog));
        }

        options.add(new Option(R.string.remove_from_group, user -> {
            dm.add(ChatSDK.thread().removeUsersFromThread(thread, user).subscribe(() -> {
                ToastHelper.show(getActivity(), R.string.success);
            }, toastOnErrorConsumer()));
        }));

    }

    protected void showRoleListDialog(User user) {

        if (rolesDialog != null) {
            rolesDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.change_role);

        final List<String> roles = ChatSDK.thread().availableRoles(thread);
        final String currentRole = ChatSDK.thread().roleForUser(thread, user);
        int checked = roles.indexOf(currentRole);

        builder.setSingleChoiceItems(ChatSDK.thread().availableRoles(thread).toArray(new String[0]), checked, (dialog, which) -> {
            String newRole = roles.get(which);
            if (!newRole.equals(currentRole)) {
                dm.add(ChatSDK.thread().setRole(newRole, thread, user).subscribe(() -> {
                    //
                    ToastHelper.show(getActivity(), R.string.success);
                }, toastOnErrorConsumer()));
            }
        });

        rolesDialog = builder.show();
    }

    protected void showUserDialog(User u) {

        if (userDialog != null) {
            userDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (getActivity() != null) {

            ArrayList<String> optionStrings = new ArrayList<>();
            for (Option o: options) {
                optionStrings.add(o.getText(getActivity()));
            }

            builder.setTitle(getActivity().getString(R.string.actions)).setItems(optionStrings.toArray(new String[0]), (dialog, which) -> {
                try {
                    options.get(which).action.accept(u);
                } catch (Exception e) {
                    ToastHelper.show(getActivity(), e.getLocalizedMessage());
                }
            });
        }
        userDialog = builder.show();
    }

    protected void showProfile(User user) {
        ChatSDK.ui().startProfileActivity(getContext(), user.getEntityID());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(activityLayout(), null);

        initViews();

        loadData(true);

        return mainView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }
    }

    protected @LayoutRes
    int activityLayout() {
        return R.layout.fragment_contacts;
    }

    public void initViews() {
        recyclerView = mainView.findViewById(R.id.recycler_contacts);

        progressBar = mainView.findViewById(R.id.progress_bar);

        // Create the adapter only if null this is here so we wont
        // override the adapter given from the extended class with setAdapter.
        adapter = new UsersListAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        dm.add(adapter.onClickObservable().subscribe(o -> {
            if (o instanceof User) {
                User user = (User) o;
                onClickSubject.onNext(user);

                if (options.size() == 1) {
                    options.get(0).action.accept(user);
                } else {
                    showUserDialog(user);
                }

            }
        }));

        dm.add(adapter.onLongClickObservable().subscribe(o -> {
            if (o instanceof User) {
                User user = (User) o;
                onLongClickSubject.onNext(user);
            }
        }));
    }

    public void loadData (final boolean force) {
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
        // If this is not a dialog we will load the contacts of the user.
        if (thread != null) {
            // Remove the current user from the list.
            List<User> users = thread.getUsers();
            for (User u : users) {
                if (!u.isMe()) {
                    sourceUsers.add(u);
                }
            }
        }
    }


}
