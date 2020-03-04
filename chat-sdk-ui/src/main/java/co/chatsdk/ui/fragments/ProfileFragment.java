package co.chatsdk.ui.fragments;

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
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.AvailabilityHelper;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.views.IconItemView;
import co.chatsdk.ui.views.SwitchItemView;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/15/17.
 */

public class ProfileFragment extends BaseFragment {

    protected User user;
    protected boolean startingChat = false;

    @BindView(R2.id.backdrop)
    ImageView backdrop;
    @BindView(R2.id.toolbar)
    Toolbar toolbar;
    @BindView(R2.id.titleTextView)
    TextView titleTextView;
    @BindView(R2.id.collapsingToolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R2.id.appbar)
    AppBarLayout appbar;
    @BindView(R2.id.topSpace)
    Space topSpace;
    @BindView(R2.id.statusTitleTextView)
    TextView statusTitleTextView;
    @BindView(R2.id.statusTextView2)
    TextView statusTextView2;
    @BindView(R2.id.statusLinearLayout)
    LinearLayout statusLinearLayout;
    @BindView(R2.id.statusCardView)
    CardView statusCardView;
    @BindView(R2.id.availabilityLinearLayout)
    LinearLayout availabilityLinearLayout;
    @BindView(R2.id.availabilityCardView)
    CardView availabilityCardView;
    @BindView(R2.id.iconLinearLayout)
    LinearLayout iconLinearLayout;
    @BindView(R2.id.buttonsLinearLayout)
    LinearLayout buttonsLinearLayout;
    @BindView(R2.id.fab)
    FloatingActionButton fab;
    @BindView(R2.id.avatarImageView)
    CircleImageView avatarImageView;
    @BindView(R2.id.onlineIndicator)
    View onlineIndicator;
    @BindView(R2.id.avatarContainerLayout)
    RelativeLayout avatarContainerLayout;
    @BindView(R2.id.root)
    CoordinatorLayout root;

    public static ProfileFragment newInstance(User user) {
        ProfileFragment f = new ProfileFragment();

        Bundle b = new Bundle();

        if (user != null) {
            b.putString(Keys.UserId, user.getEntityID());
        }

        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    protected @LayoutRes
    int getLayout() {
        return R.layout.fragment_profile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.getString(Keys.UserId) != null) {
            user = ChatSDK.db().fetchUserWithEntityID(savedInstanceState.getString(Keys.UserId));
        }

        initViews();
        addListeners();

        return view;
    }

    public void addListeners() {
        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkEvent -> {
                    if (networkEvent.user.equals(getUser())) {
                        reloadData();
                    }
                }));
        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkEvent -> {
                    if (networkEvent.user.equals(getUser())) {
                        reloadData();
                    }
                }));
    }

    public void initViews() {

        setupTouchUIToDismissKeyboard(rootView, R.id.avatarImageView);

        if (ChatSDK.profilePictures() != null) {
            avatarImageView.setOnClickListener(v -> {
                ChatSDK.profilePictures().startProfilePicturesActivity(getContext(), getUser().getEntityID());
            });
        }

        backdrop.setImageResource(R.drawable.header2);

        appbar.addOnOffsetChangedListener(new ProfileViewOffsetChangeListener(avatarContainerLayout));

        reloadData();
    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) view.setVisibility(visibility);
    }

    protected void setViewVisibility(View view, boolean visible) {
        setViewVisibility(view, visible ? View.VISIBLE : View.INVISIBLE);
    }

    protected void setViewText(TextView textView, String text) {
        if (textView != null) textView.setText(text);
    }

    protected void setRowVisible(View imageView, View textView, boolean visible) {
        setViewVisibility(textView, visible);
        setViewVisibility(imageView, visible);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void block() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.blocking().blockUser(getUser().getEntityID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    showSnackbar(R.string.user_blocked);
                }, this));
    }

    protected void unblock() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.blocking().unblockUser(getUser().getEntityID())
                .observeOn(AndroidSchedulers.mainThread())
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    showSnackbar(R.string.contact_added);
                }, this));
    }

    protected void delete() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.contact().deleteContact(getUser(), ConnectionType.Contact)
                .observeOn(AndroidSchedulers.mainThread())
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

        if (isCurrentUser) {
            fab.setImageDrawable(Icons.get(Icons.choose().edit, R.color.white));
            fab.setOnClickListener(v -> {
                showEditProfileScreen();
            });
            onlineIndicator.setVisibility(View.GONE);
        } else {
            fab.setImageDrawable(Icons.get(Icons.choose().chat, R.color.white));
            fab.setOnClickListener(v -> {
                startChat();
            });
            onlineIndicator.setVisibility(View.VISIBLE);

            if (user.getIsOnline()) {
                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online_big);
            } else {
                onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline_big);
            }
        }

        collapsingToolbar.setTitle(user.getName());
        Picasso.get().load(user.getAvatarURL()).into(avatarImageView);

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
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getLocation(), Icons.get(Icons.choose().location, R.color.profile_icon_color)));
        }
        if (!StringChecker.isNullOrEmpty(user.getPhoneNumber())) {
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getPhoneNumber(), Icons.get(Icons.choose().phone, R.color.profile_icon_color)));
        }
        if (!StringChecker.isNullOrEmpty(user.getEmail())) {
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getEmail(), Icons.get(Icons.choose().email, R.color.profile_icon_color)));
        }
        if (!StringChecker.isNullOrEmpty(user.getPresenceSubscription())) {
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getPresenceSubscription(), Icons.get(Icons.choose().check, R.color.profile_icon_color)));
            iconLinearLayout.addView(IconItemView.create(getContext(), user.getPresenceSubscription(), Icons.get(Icons.choose().check, R.color.profile_icon_color)));
        }

        if (!isCurrentUser) {

            if (ChatSDK.blocking() != null) {
                boolean isBlocked = ChatSDK.blocking().isBlocked(getUser().getEntityID());

                buttonsLinearLayout.addView(SwitchItemView.create(
                        getContext(),
                        R.string.blocked,
                        Icons.get(Icons.choose().block, R.color.blocked_primary_icon_color),
                        isBlocked,
                        R.color.blocked_primary_icon_color, R.color.blocked_secondary_icon_color, (buttonView, isChecked) -> {
                            toggleBlocked();
                        }));
            }

            boolean isContact = ChatSDK.contact().exists(getUser());

            buttonsLinearLayout.addView(SwitchItemView.create(
                    getContext(),
                    R.string.contact,
                    Icons.get(Icons.choose().contact, R.color.contacts_primary_color),
                    isContact,
                    R.color.contacts_primary_color, R.color.contacts_secondary_color, (buttonView, isChecked) -> {
                        toggleContact();
                    }));

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
        }
    }

    protected User getUser() {
        return user != null ? user : ChatSDK.currentUser();
    }

    public void showEditProfileScreen() {

//        Bundle bundle = null;
//        if (getActivity() != null) {
//            Pair<View, String> p1 = Pair.create(b.avatarImageView, "avatarImageView");
//            Pair<View, String> p2 = Pair.create(b.titleTextView, "titleTextView");
//            Pair<View, String> p3 = Pair.create(b.fab, "doneFab");
//            bundle = ActivityOptionsCompat.
//                    makeSceneTransitionAnimation(getActivity(), p1, p2, p3).toBundle();
//        }

        ChatSDK.ui().startEditProfileActivity(getContext(), ChatSDK.currentUserID());
    }

    public void startChat() {
//        showProgressDialog(getString(R.string.creating_thread));

        dm.add(ChatSDK.thread().createThread("", user, ChatSDK.currentUser())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
//                    dismissProgressDialog();
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
    }

}
