package sdk.chat.ui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.UserListItemConverter;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.RX;

public class ThreadUsersFragment extends BaseFragment {

    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;
    @BindView(R2.id.root) protected FrameLayout root;

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

    protected UsersListAdapter adapter;

    protected PublishRelay<User> onClickRelay = PublishRelay.create();
    protected PublishRelay<User> onLongClickRelay = PublishRelay.create();

    protected List<User> sourceUsers = new ArrayList<>();

    protected Thread thread;

    protected AlertDialog userDialog;
    protected AlertDialog rolesDialog;

    public ThreadUsersFragment(Thread thread) {
        this.thread = thread;
    }

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_contacts;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (thread == null) {
            String threadEntityID = savedInstanceState.getString(Keys.IntentKeyThreadEntityID);
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        }

        initViews();
        addListeners();

        loadData(true);

        return view;
    }

    public void addListeners() {
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterContactsChanged())
                .subscribe(networkEvent -> loadData(true)));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated, EventType.ThreadUserRoleUpdated))
                .subscribe(networkEvent -> loadData(true)));
    }

    public void initViews() {

        // Create the adapter only if null this is here so we wont
        // override the adapter given from the extended class with setAdapter.
        adapter = new UsersListAdapter(null, false, user -> {
            if (ChatSDK.thread().rolesEnabled(thread) && user instanceof User) {
                String role = ChatSDK.thread().roleForUser(thread, (User) user);
                if (role != null) {
                    return ChatSDK.thread().localizeRole(role);
                }
            }
            return user.getStatus();
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        dm.add(adapter.onClickObservable().subscribe(o -> {
            if (o instanceof User) {
                User user = (User) o;
                onClickRelay.accept(user);

                List<Option> options = getOptionsForUser(user);

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
                onLongClickRelay.accept(user);
            }
        }));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (thread != null) {
            outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
        }
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

    protected void showRoleListDialog(User user) {

        if (rolesDialog != null) {
            rolesDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.change_role);

        final List<String> roles = ChatSDK.thread().availableRoles(thread, user);
        final List<String> localizedRoles = ChatSDK.thread().localizeRoles(roles);

        final String currentRole = ChatSDK.thread().roleForUser(thread, user);
        int checked = roles.indexOf(currentRole);

        builder.setSingleChoiceItems(localizedRoles.toArray(new String[0]), checked, (dialog, which) -> {
            String newRole = roles.get(which);
            if (!newRole.equals(currentRole)) {
                dm.add(ChatSDK.thread().setRole(newRole, thread, user).observeOn(RX.main()).subscribe(() -> {
                    ToastHelper.show(getActivity(), R.string.success);
                }, this));
            }
            dialog.dismiss();
        });

        rolesDialog = builder.show();
    }

    protected void showUserDialog(User user) {

        if (userDialog != null) {
            userDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (getActivity() != null) {

            List<Option> options = getOptionsForUser(user);

            ArrayList<String> optionStrings = new ArrayList<>();
            for (Option o : options) {
                optionStrings.add(o.getText(getActivity()));
            }

            builder.setTitle(getActivity().getString(R.string.actions)).setItems(optionStrings.toArray(new String[0]), (dialog, which) -> {
                try {
                    options.get(which).action.accept(user);
                } catch (Exception e) {
                    ToastHelper.show(getActivity(), e.getLocalizedMessage());
                }
            });
        }
        userDialog = builder.show();
    }

    protected List<Option> getOptionsForUser(User user) {
        ArrayList<Option> options = new ArrayList<>();

        // Add the onClick options
        options.add(new Option(R.string.profile, this::showProfile));

        // Edit roles
        if (ChatSDK.thread().canChangeRole(thread, user)) {
            options.add(new Option(R.string.change_role, this::showRoleListDialog));
        }

        // Make a user a moderator
        if (ChatSDK.thread().canChangeModerator(thread, user)) {
            if (ChatSDK.thread().isModerator(thread, user)) {
                options.add(new Option(R.string.revoke_moderator, user1 -> {
                    dm.add(ChatSDK.thread().revokeModerator(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(getActivity(), R.string.success);
                    }, this));
                }));
            } else {
                options.add(new Option(R.string.grant_moderator, user1 -> {
                    dm.add(ChatSDK.thread().grantModerator(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(getActivity(), R.string.success);
                    }, this));
                }));
            }
        }

        if (ChatSDK.thread().canChangeVoice(thread, user)) {
            if (ChatSDK.thread().hasVoice(thread, user)) {
                options.add(new Option(R.string.revoke_voice, user1 -> {
                    dm.add(ChatSDK.thread().revokeVoice(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(getActivity(), R.string.success);
                    }, this));
                }));
            } else {
                options.add(new Option(R.string.grant_voice, user1 -> {
                    dm.add(ChatSDK.thread().grantVoice(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(getActivity(), R.string.success);
                    }, this));
                }));
            }
        }

        // Remove a user from the group
        if (ChatSDK.thread().canRemoveUsersFromThread(thread, Collections.singletonList(user))) {
            options.add(new Option(R.string.remove_from_group, u -> {
                dm.add(ChatSDK.thread().removeUsersFromThread(thread, u).observeOn(RX.main()).subscribe(() -> {
                    ToastHelper.show(getActivity(), R.string.success);
                }, this));
            }));
        }

        return options;
    }

    protected void showProfile(User user) {
        ChatSDK.ui().startProfileActivity(getContext(), user.getEntityID());
    }

}
