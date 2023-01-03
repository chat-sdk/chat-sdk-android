package sdk.chat.ui.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.ConnectionType;
import sdk.chat.core.utils.ProfileOption;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.binders.AvailabilityHelper;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.views.IconItemView;
import sdk.chat.ui.views.SwitchItemView;
import sdk.guru.common.RX;

/**
 * Created by ben on 8/15/17.
 */

public class ProfileFragment extends BaseFragment {

    protected User user;
    protected boolean startingChat = false;

    protected ImageView headerImageView;
    protected Toolbar toolbar;
    protected TextView titleTextView;
    protected CollapsingToolbarLayout collapsingToolbar;
    protected AppBarLayout appbar;
    protected Space topSpace;
    protected TextView statusTitleTextView;
    protected TextView statusTextView2;
    protected LinearLayout statusLinearLayout;
    protected CardView statusCardView;
    protected LinearLayout availabilityLinearLayout;
    protected CardView availabilityCardView;
    protected LinearLayout iconLinearLayout;
    protected LinearLayout buttonsLinearLayout;
    protected FloatingActionButton editFab;
    protected FloatingActionButton optionsFab;
    protected CircleImageView avatarImageView;
    protected View onlineIndicator;
    protected RelativeLayout avatarContainerLayout;
    protected CoordinatorLayout root;

    @Override
    protected @LayoutRes int getLayout() {
        return R.layout.fragment_profile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getString(Keys.UserId) != null) {
            user = ChatSDK.db().fetchUserWithEntityID(savedInstanceState.getString(Keys.UserId));
        }

        headerImageView = view.findViewById(R.id.headerImageView);
        toolbar = view.findViewById(R.id.toolbar);
        titleTextView = view.findViewById(R.id.titleTextView);
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar);
        appbar = view.findViewById(R.id.appbar);
        topSpace = view.findViewById(R.id.topSpace);
        statusTitleTextView = view.findViewById(R.id.statusTitleTextView);
        statusTextView2 = view.findViewById(R.id.statusTextView2);
        statusLinearLayout = view.findViewById(R.id.statusLinearLayout);
        statusCardView = view.findViewById(R.id.statusCardView);
        availabilityLinearLayout = view.findViewById(R.id.availabilityLinearLayout);
        availabilityCardView = view.findViewById(R.id.availabilityCardView);
        iconLinearLayout = view.findViewById(R.id.iconLinearLayout);
        buttonsLinearLayout = view.findViewById(R.id.buttonsLinearLayout);
        editFab = view.findViewById(R.id.editFab);
        optionsFab = view.findViewById(R.id.optionsFab);
        avatarImageView = view.findViewById(R.id.avatarImageView);
        onlineIndicator = view.findViewById(R.id.onlineIndicator);
        avatarContainerLayout = view.findViewById(R.id.avatarContainerLayout);
        root = view.findViewById(R.id.root);

        initViews();

        return view;
    }

    public void addListeners() {
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterUserEntityID(getUser().getEntityID()))
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .observeOn(RX.main())
                .subscribe(networkEvent -> {
                    reloadData();
                }));
    }

    public void initViews() {

        setupTouchUIToDismissKeyboard(rootView, R.id.avatarImageView);

        if (ChatSDK.profilePictures() != null) {
            avatarImageView.setOnClickListener(v -> {
                avatarImageView.setEnabled(false);
                ChatSDK.profilePictures().startProfilePicturesActivity(getContext(), getUser().getEntityID());
            });
        }

        appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(avatarContainerLayout));

    }

    protected void setHeaderImage(@Nullable String url) {
        // Make sure that this runs when the view has dimensions
        rootView.post(() -> {
            int profileHeader = UIModule.config().profileHeaderImage;
            if (url != null) {
                Glide.with(this)
                        .load(url)
                        .dontAnimate()
                        .override(appbar.getWidth(), appbar.getHeight())
                        .centerCrop()
                        .placeholder(profileHeader)
                        .error(profileHeader)
                        .into(headerImageView);
            } else {
                headerImageView.setImageResource(profileHeader);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        avatarImageView.setEnabled(true);
        editFab.setEnabled(true);
        updateInterface();
    }

    protected void block() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.blocking().blockUser(getUser().getEntityID())
                .observeOn(RX.main())
                .subscribe(() -> {
                    showSnackbar(R.string.user_blocked);
                }, this));
    }

    protected void unblock() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.blocking().unblockUser(getUser().getEntityID())
                .observeOn(RX.main())
                .subscribe(() -> {
                    showSnackbar(R.string.user_unblocked);
                }, this));
    }

    protected void toggleBlocked() {
        if (getUser().isMe()) return;

        boolean blocked = ChatSDK.blocking().isBlocked(getUser().getEntityID());
        if (blocked) {
            unblock();
        } else {
            block();
        }
    }

    protected void add() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.contact().addContact(getUser(), ConnectionType.Contact)
                .observeOn(RX.main())
                .subscribe(() -> {
                    showSnackbar(R.string.contact_added);
                }, this));
    }

    protected void delete() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.contact().deleteContact(getUser(), ConnectionType.Contact)
                .observeOn(RX.main())
                .subscribe(() -> {
                    showSnackbar(R.string.contact_deleted);
                }, this));
    }

    protected void toggleContact() {
        if (getUser().isMe()) return;

        boolean isContact = ChatSDK.contact().exists(getUser());
        if (isContact) {
            delete();
        } else {
            add();
        }
    }

    public void updateInterface() {

        User user = getUser();

        if (user == null) {
            return;
        }

        boolean isCurrentUser = user.isMe();
        setHasOptionsMenu(isCurrentUser);

        // Allow custom options to be added
        if (ChatSDK.ui().getProfileOptions(user).isEmpty()) {
            optionsFab.setVisibility(View.INVISIBLE);
        } else {
            optionsFab.setVisibility(View.VISIBLE);
            optionsFab.setImageDrawable(ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().options, R.color.white));
            optionsFab.setOnClickListener(v -> {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.options);

                List<String> options = new ArrayList<>();
                for (ProfileOption option: ChatSDK.ui().getProfileOptions(user)) {
                    options.add(option.getName());
                }

                builder.setItems(options.toArray(new String[0]), (dialog, which) -> {
                    ChatSDK.ui().getProfileOptions(user).get(which).execute(getActivity(), user.getEntityID());
                    dialog.dismiss();
                });

                builder.show();
            });
        }

        if (isCurrentUser) {
            editFab.setImageDrawable(ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().edit, R.color.white));
            editFab.setOnClickListener(v -> {
                editFab.setEnabled(false);
                showEditProfileScreen();
            });
            onlineIndicator.setVisibility(View.GONE);
        } else {
            editFab.setImageDrawable(ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().chat, R.color.white));
            editFab.setOnClickListener(v -> {
                startChat();
            });
            onlineIndicator.setVisibility(View.VISIBLE);

            if (user.getIsOnline()) {
                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online_big);
            } else {
                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline_big);
            }
        }

        setHeaderImage(user.getHeaderURL());

        collapsingToolbar.setTitle(user.getName());
        Glide.with(this).load(user.getAvatarURL()).dontAnimate().placeholder(UIModule.config().defaultProfilePlaceholder).into(avatarImageView);

        if (StringChecker.isNullOrEmpty(user.getStatus())) {
            statusCardView.setVisibility(View.GONE);
            topSpace.setVisibility(View.VISIBLE);
        } else {
            topSpace.setVisibility(View.GONE);
            statusCardView.setVisibility(View.VISIBLE);
            statusTextView2.setText(user.getStatus());
        }

        // Remove the views and add them back in
        iconLinearLayout.removeAllViews();
        availabilityLinearLayout.removeAllViews();
        buttonsLinearLayout.removeAllViews();

        if (!StringChecker.isNullOrEmpty(user.getLocation())) {
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getLocation(), ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().location, R.color.profile_icon_color)));
        }
        if (!StringChecker.isNullOrEmpty(user.getPhoneNumber())) {
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getPhoneNumber(), ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().phone, R.color.profile_icon_color)));
        }
        if (!StringChecker.isNullOrEmpty(user.getEmail())) {
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getEmail(), ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().email, R.color.profile_icon_color)));
        }

        String availability = getUser().getAvailability();

        if (!StringChecker.isNullOrEmpty(availability)) {
            availabilityCardView.setVisibility(View.VISIBLE);
            availabilityLinearLayout.addView(IconItemView.create(
                    getContext(),
                    AvailabilityHelper.stringForAvailability(getContext(), availability),
                    AvailabilityHelper.imageResourceIdForAvailability(availability)));
        } else {
            availabilityCardView.setVisibility(View.GONE);
        }

        if (!isCurrentUser) {

            if (ChatSDK.blocking() != null && ChatSDK.blocking().blockingSupported()) {
                boolean isBlocked = ChatSDK.blocking().isBlocked(getUser().getEntityID());

                buttonsLinearLayout.addView(SwitchItemView.create(
                        getContext(),
                        R.string.blocked,
                        ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().block, R.color.blocked_primary_icon_color),
                        isBlocked,
                        R.color.blocked_primary_icon_color, R.color.blocked_secondary_icon_color, (buttonView, isChecked) -> {
                            toggleBlocked();
                        }));
            }

            boolean isContact = ChatSDK.contact().exists(getUser());

            buttonsLinearLayout.addView(SwitchItemView.create(
                    getContext(),
                    R.string.contact,
                    ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().contact, R.color.contacts_primary_color),
                    isContact,
                    R.color.contacts_primary_color, R.color.contacts_secondary_color, (buttonView, isChecked) -> {
                        toggleContact();
                    }));

        }
    }

    protected User getUser() {
        return user;
    }

    public void showEditProfileScreen() {
        ChatSDK.ui().startEditProfileActivity(getContext(), ChatSDK.currentUserID());
    }

    public void startChat() {
        dm.add(ChatSDK.thread().createThread("", user, ChatSDK.currentUser())
                .observeOn(RX.main())
                .doFinally(() -> {
                    startingChat = false;
                })
                .subscribe(thread -> {
                    ChatSDK.ui().startChatActivityForID(getContext(), thread.getEntityID());
                }, this.snackbarOnErrorConsumer()));


    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {
        updateInterface();
    }

    public void setUser(User user) {
        this.user = user;
        addListeners();
        reloadData();
    }

}
