package co.chatsdk.ui.fragments;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.databinding.DataBindingUtil;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.databinding.FragmentProfileBinding;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.AvailabilityHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/15/17.
 */

public class ProfileFragment extends BaseFragment {

    public static int ProfileDetailRowHeight = 25;
    public static int ProfileDetailMargin = 8;

    protected User user;

    protected FragmentProfileBinding b;

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

    protected @LayoutRes int getLayout() {
        return R.layout.fragment_profile;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        b = DataBindingUtil.inflate(inflater, getLayout(), container, false);
        rootView = b.getRoot();

        if (savedInstanceState != null && savedInstanceState.getString(Keys.UserId) != null) {
            user = ChatSDK.db().fetchUserWithEntityID(savedInstanceState.getString(Keys.UserId));
        }

        initViews();
        addListeners();

        return rootView;
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

        setupIcons();

        if (ChatSDK.profilePictures() != null) {
            b.avatarImageView.setOnClickListener(v -> {
                ChatSDK.profilePictures().startProfilePicturesActivity(getContext(), getUser().getEntityID());
            });
        }

        reloadData();
    }

    public void setupIcons() {
        b.emailImageView.setImageDrawable(Icons.get(Icons.shared().email, R.color.profile_icon_color));
        b.phoneImageView.setImageDrawable(Icons.get(Icons.shared().phone, R.color.profile_icon_color));
        b.locationImageView.setImageDrawable(Icons.get(Icons.shared().location, R.color.profile_icon_color));
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

    protected void setRowVisible (View imageView, View textView, boolean visible) {
        setViewVisibility(textView, visible);
        setViewVisibility(imageView, visible);
    }

    protected void updateBlockedButton(boolean blocked) {
        if (blocked) {
            setViewText(b.blockButton, getString(R.string.unblock));
        } else {
            setViewText(b.blockButton, getString(R.string.block));
        }
    }

    protected void updateFriendsButton(boolean friend) {
        if (friend) {
            setViewText(b.addDeleteButton, getString(R.string.delete_contact));
        } else {
            setViewText(b.addDeleteButton, getString(R.string.add_contacts));
        }
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
                    updateBlockedButton(true);
                    updateInterface();
                    ToastHelper.show(getContext(), getString(R.string.user_blocked));
                },this));
    }

    protected void unblock() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.blocking().unblockUser(getUser().getEntityID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateBlockedButton(false);
                    updateInterface();
                    ToastHelper.show(getContext(), R.string.user_unblocked);
                }, this));
    }

    protected void toggleBlocked() {
        if (getUser().isMe()) return;

        boolean blocked = ChatSDK.blocking().isBlocked(getUser().getEntityID());
        if (blocked) unblock();
        else block();
    }

    protected void add() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.contact().addContact(getUser(), ConnectionType.Contact)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateFriendsButton(true);
                    ToastHelper.show(getContext(), getString(R.string.contact_added));
                }, this));
    }

    protected void delete() {
        if (getUser().isMe()) return;

        dm.add(ChatSDK.contact().deleteContact(getUser(), ConnectionType.Contact)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateFriendsButton(false);
                    ToastHelper.show(getContext(), getString(R.string.contact_deleted));
                    getActivity().finish();
                }, this));
    }

    protected void toggleFriends() {
        if (getUser().isMe()) return;

        boolean friends = ChatSDK.contact().exists(getUser());
        if (friends) delete();
        else add();
    }

    public void updateInterface() {

        User user = getUser();

        if (user == null) return;
        //this.user = user;

        boolean isCurrentUser = user.isMe();
        setHasOptionsMenu(isCurrentUser);

        boolean visible = !isCurrentUser;

        setViewVisibility(b.followsImageView, visible);
        setViewVisibility(b.followedImageView, visible);
        setViewVisibility(b.followsTextView, visible);
        setViewVisibility(b.followedTextView, visible);
        setViewVisibility(b.blockButton, visible);
        setViewVisibility(b.addDeleteButton, visible);

        setRowVisible(b.locationImageView, b.locationTextView, !StringChecker.isNullOrEmpty(user.getLocation()));
        setRowVisible(b.phoneImageView, b.phoneTextView, !StringChecker.isNullOrEmpty(user.getPhoneNumber()));
        setRowVisible(b.emailImageView, b.emailTextView, !StringChecker.isNullOrEmpty(user.getEmail()));
        setRowVisible(b.followsImageView, b.followedTextView, !StringChecker.isNullOrEmpty(user.getPresenceSubscription()));
        setRowVisible(b.followedImageView, b.followedTextView, !StringChecker.isNullOrEmpty(user.getPresenceSubscription()));

        if (!isCurrentUser) {
            // Find out if the user is blocked already?
            if (ChatSDK.blocking() != null && ChatSDK.blocking().blockingSupported()) {
                updateBlockedButton(ChatSDK.blocking().isBlocked(getUser().getEntityID()));
                if (b.blockButton != null) b.blockButton.setOnClickListener(v -> toggleBlocked());
            }
            else {
                // TODO: Set height to zero
                setViewVisibility(b.blockButton, false);
            }

            updateFriendsButton(ChatSDK.contact().exists(getUser()));
            if (b.addDeleteButton != null) b.addDeleteButton.setOnClickListener(view -> toggleFriends());
        }

        // Country Flag
        String countryCode = getUser().getCountryCode();
        setViewVisibility(b.flagImageView, false);

        if (countryCode != null && !countryCode.isEmpty()) {
            int flagResourceId = getFlagResId(countryCode);
            if (b.flagImageView != null && flagResourceId >= 0) {
                b.flagImageView.setImageResource(flagResourceId);
                setViewVisibility(b.flagImageView, true);
            }
        }

        // Profile Image
        if (b.avatarImageView != null) {
            int width = Dimen.from(R.dimen.small_avatar_width);
            int height = Dimen.from(R.dimen.small_avatar_height);
            getUser().loadAvatar(b.avatarImageView, width, height);
        }

        String status = getUser().getStatus();
        if (!StringChecker.isNullOrEmpty(status)) {
            setViewText(b.statusTextView, status);
        } else {
            setViewText(b.statusTextView, "");
        }

        // Name
        setViewText(b.nameTextView, getUser().getName());

        String availability = getUser().getAvailability();

        // Availability
        if (availability != null && !isCurrentUser && b.availabilityImageView != null) {
            b.availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(availability));
            setViewVisibility(b.availabilityImageView, true);
        } else {
            setViewVisibility(b.availabilityImageView, false);
        }

        // Location
        setViewText(b.locationTextView, getUser().getLocation());

        // Phone
        setViewText(b.phoneTextView, getUser().getPhoneNumber());

        // Email
        setViewText(b.emailTextView, getUser().getEmail());

        ConstraintLayout layout = rootView.findViewById(R.id.mainConstraintLayout);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);

        ArrayList<View> imageViewIds = new ArrayList<>();
        imageViewIds.add(b.locationImageView);
        imageViewIds.add(b.phoneImageView);
        imageViewIds.add(b.emailImageView);
        imageViewIds.add(b.followsImageView);
        imageViewIds.add(b.followedImageView);

        stackViews(imageViewIds, b.statusTextView, set);

        ArrayList<View> textViewIds = new ArrayList<>();
        textViewIds.add(b.locationTextView);
        textViewIds.add(b.phoneTextView);
        textViewIds.add(b.emailTextView);
        textViewIds.add(b.followsTextView);
        textViewIds.add(b.followedTextView);
        textViewIds.add(b.addDeleteButton);
        textViewIds.add(b.blockButton);

        stackViews(textViewIds, b.statusTextView, set);

        set.applyTo(layout);
    }

    protected void stackViews (ArrayList<View> views, View firstView, ConstraintSet set) {
        int lastViewId = firstView.getId();
        final float density = getContext().getResources().getDisplayMetrics().density;
        for (View view: views) {
            if (view != null && view.getVisibility() == View.VISIBLE) {
                set.connect(view.getId(), ConstraintSet.TOP, lastViewId, ConstraintSet.BOTTOM, (int) (ProfileDetailMargin * density));
                //set.constrainHeight(viewId, ProfileDetailRowHeight * density);
                lastViewId = view.getId();
            }
        }
    }

    protected User getUser () {
        return user != null ? user : ChatSDK.currentUser();
    }

    /**
     * The drawable image name has the format "flag_$countryCode". We need to
     * load the drawable dynamically from country code. Code from
     * http://stackoverflow.com/
     * questions/3042961/how-can-i-get-the-resource-id-of
     * -an-image-if-i-know-its-name
     *
     * @param countryCode
     * @return
     */
    public static int getFlagResId(String countryCode) {
        String drawableName = "flag_"
                + countryCode.toLowerCase(Locale.ENGLISH);

        try {
            Class<R.drawable> res = R.drawable.class;
            Field field = res.getField(drawableName);
            return field.getInt(null);
        } catch (Exception e) {
            ChatSDK.logError(e);
        }
        return -1;
    }

    public void showSettings() {
        ChatSDK.ui().startEditProfileActivity(getContext(), ChatSDK.currentUserID());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!getUser().isMe())
            return;

        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_settings, 12, getString(R.string.action_settings));
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icn_24_settings);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_settings)
        {
            showSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {
        updateInterface();
    }

    public void setUser (User user) {
        this.user = user;
    }
}
