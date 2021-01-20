/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package sdk.chat.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jakewharton.rxrelay2.PublishRelay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.functions.Consumer;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.defines.Availability;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.core.utils.Strings;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.fragments.ProfileViewOffsetChangeListener;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ThreadImageBuilder;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.RX;

/**
 * Created by Ben Smiley on 24/11/14.
 */
public class ThreadDetailsActivity extends ImagePreviewActivity {

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

    protected ActionBar actionBar;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.avatarImageView) protected CircleImageView avatarImageView;

    @BindView(R2.id.addUsersFab) protected FloatingActionButton addUsersFab;
    @BindView(R2.id.refreshFab) protected FloatingActionButton refreshFab;
    @BindView(R2.id.headerImageView) protected ImageView headerImageView;
    @BindView(R2.id.recyclerView) protected RecyclerView recyclerView;
    @BindView(R2.id.appbar) protected AppBarLayout appbar;
    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;

    protected Thread thread;

    protected UsersListAdapter adapter;

    protected PublishRelay<User> onClickRelay = PublishRelay.create();
    protected PublishRelay<User> onLongClickRelay = PublishRelay.create();

    protected List<UserListItem> members = new ArrayList<>();

    protected AlertDialog userDialog;
    protected AlertDialog rolesDialog;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.activity_thread_details;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            getDataFromBundle(savedInstanceState);
        } else {
            if (getIntent().getExtras() != null) {
                getDataFromBundle(getIntent().getExtras());
            } else {
                finish();
            }
        }
        if (thread == null) {
            ToastHelper.show(this, R.string.error_thread_not_found);
            finish();
        }

        initViews();

    }

    protected void initViews() {
        super.initViews();

        appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(avatarImageView));
        appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(onlineIndicator));

        if (thread.typeIs(ThreadType.Group)) {
            onlineIndicator.setVisibility(View.INVISIBLE);
        } else {
            onlineIndicator.setVisibility(View.VISIBLE);
        }

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

        adapter.setRowBinder((holder, item) -> {
            holder.bind(item);
            if (item instanceof User) {
                User user = (User) item;
                UserThreadLink link = thread.getUserThreadLink(user.getId());
                if (link.isActive()) {
                    holder.setAvailability(Availability.Available);
                } else {
                    holder.setAvailability(Availability.Unavailable);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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


        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadUserAdded))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    adapter.addUser(networkEvent.getUser(), -1, true, true);
                }, this));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadUserRemoved))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    adapter.removeUser(networkEvent.getUser());
                }, this));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated))
                .filter(NetworkEvent.filterThreadEntityID(thread.getEntityID()))
                .subscribe(networkEvent -> {
                    reloadInterface();
                }, this));


        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterRoleUpdated(thread))
                .subscribe(networkEvent -> adapter.updateUser(networkEvent.getUser()), this));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterPresence(thread))
                .subscribe(networkEvent -> {
                    adapter.updateUser(networkEvent.getUser());
                    if (thread.typeIs(ThreadType.Private1to1)) {
                        UIModule.shared().getOnlineStatusBinder().bind(onlineIndicator, thread.otherUser().getIsOnline());
                    }
                }));

        if (ChatSDK.thread().canAddUsersToThread(thread)) {
            addUsersFab.setImageDrawable(Icons.get(this, Icons.choose().add, R.color.white));
            addUsersFab.setOnClickListener(v -> {
                addUsersFab.setEnabled(false);
                ChatSDK.ui().startAddUsersToThreadActivity(this, thread.getEntityID());
            });
        } else {
            addUsersFab.setVisibility(View.INVISIBLE);
        }

        if (ChatSDK.thread().canRefreshRoles(thread)) {
            refreshFab.setImageDrawable(Icons.get(this, Icons.choose().refresh, R.color.white));
            refreshFab.setOnClickListener(v -> {
                ChatSDK.thread().refreshRoles(thread).subscribe();
            });
        } else {
            refreshFab.setVisibility(View.INVISIBLE);
        }

        int profileHeader = UIModule.config().profileHeaderImage;
        headerImageView.setImageResource(profileHeader);

        ChatSDK.thread().refreshRoles(thread).subscribe();

        reloadData(true);
        reloadInterface();
    }

    public void reloadInterface() {
        actionBar = getSupportActionBar();
        String name = Strings.nameForThread(thread);
        if (actionBar != null) {
            actionBar.setTitle(name);
            actionBar.setHomeButtonEnabled(true);
        }

        if (!StringChecker.isNullOrEmpty(thread.getImageUrl())) {
            avatarImageView.setOnClickListener(v -> zoomImageFromThumbnail(avatarImageView, thread.getImageUrl()));
            Glide.with(this).load(thread.getImageUrl()).dontAnimate().into(avatarImageView);
        } else {
            ThreadImageBuilder.load(avatarImageView, thread, Dimen.from(this, R.dimen.large_avatar_width));
            avatarImageView.setOnClickListener(null);
        }
    }

    public void reloadData(final boolean force) {

        final ArrayList<UserListItem> originalUserList = new ArrayList<>(members);

        members.clear();

        // If this is not a dialog we will load the contacts of the user.
        members.addAll(thread.getMembers());

        if (!originalUserList.equals(members) || force) {
            adapter.setUsers(members, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ChatSDK.thread().canRefreshRoles(thread)) {
            ChatSDK.thread().refreshRoles(thread).subscribe();
        }
//        reloadData(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        finish(); // Finish needs to be called before animate exit
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO: Enable thread images
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        getDataFromBundle(intent.getExtras());
    }

    protected void getDataFromBundle(@Nullable Bundle bundle) {
        if (bundle == null) {
            return;
        }

        String threadEntityID = bundle.getString(Keys.IntentKeyThreadEntityID);

        if (threadEntityID != null && !threadEntityID.isEmpty()) {
            thread = ChatSDK.db().fetchThreadWithEntityID(threadEntityID);
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.IntentKeyThreadEntityID, thread.getEntityID());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_thread_details_menu, menu);

        // Only the creator can modify the group. Also, private 1-to-1 chats can't be edited
        if (!ChatSDK.thread().canEditThreadDetails(thread)) {
            menu.removeItem(R.id.action_edit);
        }

        if (!ChatSDK.thread().muteEnabled(thread)) {
            menu.removeItem(R.id.action_mute);
        }

        if (!ChatSDK.thread().canLeaveThread(thread)) {
            menu.removeItem(R.id.action_leave);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_edit) {
            ChatSDK.ui().startEditThreadActivity(this, thread.getEntityID());
        }
        if (item.getItemId() == R.id.action_mute) {
            if (thread.isMuted()) {
                ChatSDK.thread().unmute(thread).subscribe(this);
            } else {
                ChatSDK.thread().mute(thread).subscribe(this);
            }
            invalidateOptionsMenu();
        }
        if (item.getItemId() == R.id.action_leave) {
            ChatSDK.thread().leaveThread(thread).doOnComplete(this::finish).subscribe(this);
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_mute);

        if (item != null) {
            String muteText = getApplicationContext().getString(R.string.mute_notifications);
            String unmuteText = getApplicationContext().getString(R.string.unmute_notifications);

            if (thread.isMuted()) {
                item.setTitle(unmuteText);
            } else {
                item.setTitle(muteText);
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    protected void showRoleListDialog(User user) {

        ChatSDK.ui().startModerationActivity(this, thread.getEntityID(), user.getEntityID());

        return;
//        if (rolesDialog != null) {
//            rolesDialog.dismiss();
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(R.string.change_role);
//
//        final List<String> roles = ChatSDK.thread().availableRoles(thread, user);
//        final List<String> localizedRoles = ChatSDK.thread().localizeRoles(roles);
//
//        final String currentRole = ChatSDK.thread().roleForUser(thread, user);
//        int checked = roles.indexOf(currentRole);
//
//        builder.setSingleChoiceItems(localizedRoles.toArray(new String[0]), checked, (dialog, which) -> {
//            String newRole = roles.get(which);
//            if (!newRole.equals(currentRole)) {
//                dm.add(ChatSDK.thread().setRole(newRole, thread, user).observeOn(RX.main()).subscribe(() -> {
//                    ToastHelper.show(this, R.string.success);
//                }, this));
//            }
//            dialog.dismiss();
//        });
//
//        rolesDialog = builder.show();
    }

    protected void showUserDialog(User user) {

        ChatSDK.ui().startModerationActivity(this, thread.getEntityID(), user.getEntityID());


//
//        if (userDialog != null) {
//            userDialog.dismiss();
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//        List<Option> options = getOptionsForUser(user);
//
//        ArrayList<String> optionStrings = new ArrayList<>();
//        for (Option o : options) {
//            optionStrings.add(o.getText(this));
//        }
//
//        builder.setTitle(this.getString(R.string.actions)).setItems(optionStrings.toArray(new String[0]), (dialog, which) -> {
//            try {
//                options.get(which).action.accept(user);
//            } catch (Exception e) {
//                ToastHelper.show(this, e.getLocalizedMessage());
//            }
//        });
//
//        userDialog = builder.show();
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
                        ToastHelper.show(this, R.string.success);
                    }, this));
                }));
            } else {
                options.add(new Option(R.string.grant_moderator, user1 -> {
                    dm.add(ChatSDK.thread().grantModerator(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(this, R.string.success);
                    }, this));
                }));
            }
        }

        if (ChatSDK.thread().canChangeVoice(thread, user)) {
            if (ChatSDK.thread().hasVoice(thread, user)) {
                options.add(new Option(R.string.revoke_voice, user1 -> {
                    dm.add(ChatSDK.thread().revokeVoice(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(this, R.string.success);
                    }, this));
                }));
            } else {
                options.add(new Option(R.string.grant_voice, user1 -> {
                    dm.add(ChatSDK.thread().grantVoice(thread, user1).observeOn(RX.main()).subscribe(() -> {
                        ToastHelper.show(this, R.string.success);
                    }, this));
                }));
            }
        }

        // Remove a user from the group
        if (ChatSDK.thread().canRemoveUsersFromThread(thread, Collections.singletonList(user))) {
            options.add(new Option(R.string.remove_from_group, u -> {
                dm.add(ChatSDK.thread().removeUsersFromThread(thread, u).observeOn(RX.main()).subscribe(() -> {
                    ToastHelper.show(this, R.string.success);
                }, this));
            }));
        }

        return options;
    }

    protected void showProfile(User user) {
        ChatSDK.ui().startProfileActivity(this, user.getEntityID());
    }
}
