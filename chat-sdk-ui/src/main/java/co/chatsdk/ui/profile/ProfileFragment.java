package co.chatsdk.ui.profile;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseFragment;
import co.chatsdk.ui.utils.AvailabilityHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by ben on 8/15/17.
 */

public class ProfileFragment extends BaseFragment {

    public static int ProfileDetailRowHeight = 25;
    public static int ProfileDetailMargin = 8;

    protected SimpleDraweeView avatarImageView;
    protected ImageView flagImageView;
    protected ImageView availabilityImageView;
    protected TextView nameTextView;
    protected TextView emailTextView;
    protected TextView statusTextView;
    protected TextView locationTextView;
    protected TextView phoneTextView;
    protected TextView followsTextView;
    protected TextView followedTextView;
    protected Button blockButton;
    protected Button deleteButton;
    protected ImageView followsImageView;
    protected ImageView followedImageView;

    protected ImageView locationImageView;
    protected ImageView phoneImageView;
    protected ImageView emailImageView;

    private DisposableList disposableList = new DisposableList();

    protected User user;

    public static ProfileFragment newInstance() {
        return ProfileFragment.newInstance(null);
    }

    public static ProfileFragment newInstance(User user) {
        ProfileFragment f = new ProfileFragment();
        f.user = user;

        Bundle b = new Bundle();
        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        disposableList.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkEvent -> {
                    if (networkEvent.user.equals(getUser())) {
                        reloadData();
                    }
                }));

        initViews(inflater);

        return mainView;
    }

    public void initViews(LayoutInflater inflater) {
        mainView = inflater.inflate(R.layout.chat_sdk_profile_fragment, null);

        setupTouchUIToDismissKeyboard(mainView, R.id.ivAvatar);

        avatarImageView = mainView.findViewById(R.id.ivAvatar);
        flagImageView = mainView.findViewById(R.id.ivFlag);
        availabilityImageView = mainView.findViewById(R.id.ivAvailability);
        nameTextView = mainView.findViewById(R.id.tvName);
        statusTextView = mainView.findViewById(R.id.tvStatus);

        locationTextView = mainView.findViewById(R.id.tvLocation);
        phoneTextView = mainView.findViewById(R.id.tvPhone);
        emailTextView = mainView.findViewById(R.id.tvEmail);
        followsTextView = mainView.findViewById(R.id.tvFollows);
        followedTextView = mainView.findViewById(R.id.tvFollowed);
        blockButton = mainView.findViewById(R.id.btnBlock);
        deleteButton = mainView.findViewById(R.id.btnDelete);

//        followsHeight = followsTextView.getHeight();
//        followedHeight = followedTextView.getHeight();

        locationImageView = mainView.findViewById(R.id.ivLocation);
        phoneImageView = mainView.findViewById(R.id.ivPhone);
        emailImageView = mainView.findViewById(R.id.ivEmail);

        followsImageView = mainView.findViewById(R.id.ivFollows);
        followedImageView = mainView.findViewById(R.id.ivFollowed);

        reloadData();

        addUserMetaUpdatedEventListener();
    }

    protected void addUserMetaUpdatedEventListener() {
        disposableList.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(networkEvent -> {
                    if (networkEvent.user.equals(getUser())) {
                        reloadData();
                    }
                }));
    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) view.setVisibility(visibility);
    }

    protected void setViewText(TextView textView, String text) {
        if (textView != null) textView.setText(text);
    }

    protected void setRowVisible (int textViewID, int imageViewID, boolean visible) {
        setViewVisibility(mainView.findViewById(textViewID), visible ? View.VISIBLE : View.INVISIBLE);
        setViewVisibility(mainView.findViewById(imageViewID), visible ? View.VISIBLE : View.INVISIBLE);
    }

    protected void updateBlockedButton (boolean blocked) {
        if (blocked) {
            setViewText(blockButton, getString(R.string.unblock));
        } else {
            setViewText(blockButton, getString(R.string.block));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        updateInterface();
    }

    protected void block() {
        if (getUser().isMe()) return;

        disposableList.add(ChatSDK.blocking().blockUser(getUser().getEntityID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateBlockedButton(true);
                    updateInterface();
                    ToastHelper.show(getContext(), getString(R.string.user_blocked));
                }, throwable1 -> {
                    ChatSDK.logError(throwable1);
                    Toast.makeText(ProfileFragment.this.getContext(), throwable1.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    protected void unblock() {
        if (getUser().isMe()) return;

        disposableList.add(ChatSDK.blocking().unblockUser(getUser().getEntityID())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    updateBlockedButton(false);
                    updateInterface();
                    ToastHelper.show(getContext(), R.string.user_unblocked);
                }, throwable12 -> {
                    ChatSDK.logError(throwable12);
                    Toast.makeText(ProfileFragment.this.getContext(), throwable12.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    protected void toggleBlocked() {
        if (getUser().isMe()) return;

        boolean blocked = ChatSDK.blocking().isBlocked(getUser().getEntityID());
        if (blocked) unblock();
        else block();
    }

    protected void delete() {
        if (getUser().isMe()) return;

        disposableList.add(ChatSDK.contact().deleteContact(getUser(), ConnectionType.Contact)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    ToastHelper.show(getContext(), getString(R.string.user_deleted));
                    getActivity().finish();
                }, throwable -> {
                    ChatSDK.logError(throwable);
                    Toast.makeText(ProfileFragment.this.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    public void updateInterface() {

        User user = getUser();

        if (user == null) return;
        //this.user = user;

        boolean isCurrentUser = user.isMe();
        setHasOptionsMenu(isCurrentUser);

        int visibility = isCurrentUser ? View.INVISIBLE : View.VISIBLE;

        setViewVisibility(followsImageView, visibility);
        setViewVisibility(followedImageView, visibility);
        setViewVisibility(followsTextView, visibility);
        setViewVisibility(followedTextView, visibility);
        setViewVisibility(blockButton, visibility);
        setViewVisibility(deleteButton, visibility);

        setRowVisible(R.id.ivLocation, R.id.tvLocation, !StringChecker.isNullOrEmpty(user.getLocation()));
        setRowVisible(R.id.ivPhone, R.id.tvPhone, !StringChecker.isNullOrEmpty(user.getPhoneNumber()));
        setRowVisible(R.id.ivEmail, R.id.tvEmail, !StringChecker.isNullOrEmpty(user.getEmail()));
        setRowVisible(R.id.ivFollows, R.id.tvFollows, !StringChecker.isNullOrEmpty(user.getPresenceSubscription()));
        setRowVisible(R.id.ivFollowed, R.id.tvFollowed, !StringChecker.isNullOrEmpty(user.getPresenceSubscription()));

        if (!isCurrentUser) {
            // Find out if the user is blocked already?
            if (ChatSDK.blocking() != null && ChatSDK.blocking().blockingSupported()) {
                updateBlockedButton(ChatSDK.blocking().isBlocked(getUser().getEntityID()));
                if (blockButton != null) blockButton.setOnClickListener(v -> toggleBlocked());
            }
            else {
                // TODO: Set height to zero
                setViewVisibility(blockButton, View.INVISIBLE);
            }

            if (deleteButton != null) deleteButton.setOnClickListener(view -> delete());
        }


        // Country Flag
        String countryCode = getUser().getCountryCode();
        setViewVisibility(flagImageView, View.INVISIBLE);

        if (countryCode != null && !countryCode.isEmpty()) {
            int flagResourceId = getFlagResId(countryCode);
            if (flagImageView != null && flagResourceId >= 0) {
                flagImageView.setImageResource(flagResourceId);
                setViewVisibility(flagImageView, View.VISIBLE);
            }
        }

        // Profile Image
        if (avatarImageView != null) avatarImageView.setImageURI(getUser().getAvatarURL());

        String status = getUser().getStatus();
        if (!StringChecker.isNullOrEmpty(status)) {
            setViewText(statusTextView, status);
        } else {
            setViewText(statusTextView, "");
        }

        // Name
        setViewText(nameTextView, getUser().getName());

        String availability = getUser().getAvailability();

        // Availability
        if (availability != null && !isCurrentUser) {
            if (availabilityImageView != null) {
                availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(availability));
            }
            setViewVisibility(availabilityImageView, View.VISIBLE);
        } else {
            setViewVisibility(availabilityImageView, View.INVISIBLE);
        }

        // Location
        setViewText(locationTextView, getUser().getLocation());

        // Phone
        setViewText(phoneTextView, getUser().getPhoneNumber());

        // Email
        setViewText(emailTextView, getUser().getEmail());

//        String presenceSubscription = getUser().getPresenceSubscription();
//
//        boolean follows = false;
//        boolean followed = false;
//        if (presenceSubscription != null) {
//            follows = presenceSubscription.equals("from") || presenceSubscription.equals("both");
//            followed = presenceSubscription.equals("to") || presenceSubscription.equals("both");
//        }
//
//        if (follows) {
//            followsImageView.setMaxHeight(followsHeight);
//            followsTextView.setMaxHeight(followsHeight);
//        }
//        else {
//            followsImageView.setMaxHeight(0);
//            followsTextView.setMaxHeight(0);
//        }
//        if (followed) {
//            followedImageView.setMaxHeight(followedHeight);
//            followedTextView.setMaxHeight(followedHeight);
//        }
//        else {
//            followedImageView.setMaxHeight(0);
//            followedTextView.setMaxHeight(0);
//        }


        ConstraintLayout layout = mainView.findViewById(R.id.mainConstraintLayout);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);

        ArrayList<Integer> imageViewIds = new ArrayList<>();
        imageViewIds.add(R.id.ivLocation);
        imageViewIds.add(R.id.ivPhone);
        imageViewIds.add(R.id.ivEmail);
        imageViewIds.add(R.id.ivFollows);
        imageViewIds.add(R.id.ivFollowed);

        stackViews(imageViewIds, R.id.tvStatus, set);

        ArrayList<Integer> textViewIds = new ArrayList<>();
        textViewIds.add(R.id.tvLocation);
        textViewIds.add(R.id.tvPhone);
        textViewIds.add(R.id.tvEmail);
        textViewIds.add(R.id.tvFollows);
        textViewIds.add(R.id.tvFollowed);
        textViewIds.add(R.id.btnDelete);
        textViewIds.add(R.id.btnBlock);

        stackViews(textViewIds, R.id.tvStatus, set);

        set.applyTo(layout);
    }

    protected void stackViews (ArrayList<Integer> viewIds, Integer firstViewId, ConstraintSet set) {
        int lastViewId = firstViewId;
        final float density = getContext().getResources().getDisplayMetrics().density;
        for (int viewId : viewIds) {
            View view = mainView.findViewById(viewId);
            if (view != null && view.getVisibility() == View.VISIBLE) {
                set.connect(viewId, ConstraintSet.TOP, lastViewId, ConstraintSet.BOTTOM, (int) (ProfileDetailMargin * density));
                //set.constrainHeight(viewId, ProfileDetailRowHeight * density);
                lastViewId = viewId;
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
        ChatSDK.ui().startEditProfileActivity(getContext(), ChatSDK.currentUser().getEntityID());
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
    public void onDestroyView() {
        super.onDestroyView();
        disposableList.dispose();
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
