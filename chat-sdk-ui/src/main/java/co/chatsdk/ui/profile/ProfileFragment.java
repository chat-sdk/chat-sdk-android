package co.chatsdk.ui.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
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

import co.chatsdk.core.session.NM;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.manager.InterfaceManager;
import co.chatsdk.ui.R;
import co.chatsdk.ui.main.BaseFragment;
import co.chatsdk.ui.utils.AvailabilityHelper;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 8/15/17.
 */

public class ProfileFragment extends BaseFragment {

    public static int ProfileDetailRowHeight = 25;
    public static int ProfileDetailMargin = 8;

    private SimpleDraweeView avatarImageView;
    private ImageView flagImageView;
    private ImageView availabilityImageView;
    private TextView nameTextView;
    private TextView statusTextView;
    private TextView locationTextView;
    private TextView phoneTextView;
    private TextView followsTextView;
    private TextView followedTextView;
    private Button blockButton;
    private Button deleteButton;
    private ImageView followsImageView;
    private ImageView followedImageView;

    private ImageView locationImageView;
    private ImageView phoneImageView;

    private ArrayList<Disposable> disposables = new ArrayList<>();

    private User user;

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

        disposables.add(NM.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated, EventType.UserPresenceUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                if(networkEvent.user.equals(getUser())) {
                    reloadData();
                }
            }
        }));

        initViews(inflater);

        return mainView;
    }

    public void initViews(LayoutInflater inflater){
        mainView = inflater.inflate(R.layout.chat_sdk_profile_fragment, null);

        setupTouchUIToDismissKeyboard(mainView, R.id.ivAvatar);

        avatarImageView = (SimpleDraweeView) mainView.findViewById(R.id.ivAvatar);
        flagImageView = (ImageView) mainView.findViewById(R.id.ivFlag);
        availabilityImageView = (ImageView) mainView.findViewById(R.id.ivAvailability);
        nameTextView = (TextView) mainView.findViewById(R.id.tvName);
        statusTextView = (TextView) mainView.findViewById(R.id.tvStatus);

        locationTextView = (TextView) mainView.findViewById(R.id.tvLocation);
        phoneTextView = (TextView) mainView.findViewById(R.id.tvPhone);
        followsTextView = (TextView) mainView.findViewById(R.id.tvFollows);
        followedTextView = (TextView) mainView.findViewById(R.id.tvFollowed);
        blockButton = (Button) mainView.findViewById(R.id.btnBlock);
        deleteButton = (Button) mainView.findViewById(R.id.btnDelete);

//        followsHeight = followsTextView.getHeight();
//        followedHeight = followedTextView.getHeight();

        locationImageView = (ImageView) mainView.findViewById(R.id.ivLocation);
        phoneImageView = (ImageView) mainView.findViewById(R.id.ivPhone);
        followsImageView = (ImageView) mainView.findViewById(R.id.ivFollows);
        followedImageView = (ImageView) mainView.findViewById(R.id.ivFollowed);

        reloadData();

        disposables.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                if(networkEvent.user.equals(getUser())) {
                    reloadData();
                }
            }
        }));
    }

    private void setRowVisible (int textViewID, int imageViewID, boolean visible) {
        mainView.findViewById(textViewID).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mainView.findViewById(imageViewID).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateBlockedButton (boolean blocked) {
        if (blocked) {
            blockButton.setText(getString(R.string.unblock));
        }
        else {
            blockButton.setText(getString(R.string.block));
        }
    }

    public void updateInterface() {

        User user = getUser();

        if(user == null) {
            return;
        }
        //this.user = user;

        boolean isCurrentUser = NM.currentUser().equals(user);
        setHasOptionsMenu(isCurrentUser);

        int visibility = isCurrentUser ? View.INVISIBLE : View.VISIBLE;

        followsImageView.setVisibility(visibility);
        followedImageView.setVisibility(visibility);
        followsTextView.setVisibility(visibility);
        followedTextView.setVisibility(visibility);
        blockButton.setVisibility(visibility);
        deleteButton.setVisibility(visibility);

        setRowVisible(R.id.ivLocation, R.id.tvLocation, !StringChecker.isNullOrEmpty(user.getLocation()));
        setRowVisible(R.id.ivPhone, R.id.tvPhone, !StringChecker.isNullOrEmpty(user.getPhoneNumber()));
        setRowVisible(R.id.ivFollows, R.id.tvFollows, !StringChecker.isNullOrEmpty(user.getPresenceSubscription()));
        setRowVisible(R.id.ivFollowed, R.id.tvFollowed, !StringChecker.isNullOrEmpty(user.getPresenceSubscription()));

        if (!isCurrentUser) {
            // Find out if the user is blocked already?
            if(NM.blocking() != null && NM.blocking().blockingSupported()) {
                disposables.add(NM.blocking().isBlocked(getUser().getEntityID())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BiConsumer<Boolean, Throwable>() {
                            @Override
                            public void accept(Boolean blocked, Throwable throwable) throws Exception {
                                updateBlockedButton(blocked);
                            }
                        }));
                blockButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        disposables.add(NM.blocking().isBlocked(getUser().getEntityID())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new BiConsumer<Boolean, Throwable>() {
                                    @Override
                                    public void accept(Boolean blocked, Throwable throwable) throws Exception {
                                        if(blocked) {
                                            disposables.add(NM.blocking().unblockUser(getUser().getEntityID())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Action() {
                                                        @Override
                                                        public void run() throws Exception {
                                                            updateBlockedButton(false);
                                                            ToastHelper.show(getContext(), R.string.user_unblocked);
                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(@NonNull Throwable throwable) throws Exception {
                                                            throwable.printStackTrace();
                                                            Toast.makeText(ProfileFragment.this.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }));
                                        }
                                        else {
                                            disposables.add(NM.blocking().blockUser(getUser().getEntityID())
                                                    .observeOn(AndroidSchedulers.mainThread())
                                                    .subscribe(new Action() {
                                                        @Override
                                                        public void run() throws Exception {
                                                            updateBlockedButton(true);
                                                            ToastHelper.show(getContext(), getString(R.string.user_blocked));
                                                        }
                                                    }, new Consumer<Throwable>() {
                                                        @Override
                                                        public void accept(@NonNull Throwable throwable) throws Exception {
                                                            throwable.printStackTrace();
                                                            Toast.makeText(ProfileFragment.this.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }));
                                        }
                                    }
                                }));
                    }
                });
            }
            else {
                // TODO: Set height to zero
                blockButton.setVisibility(View.INVISIBLE);
            }

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disposables.add(NM.contact().deleteContact(getUser(), ConnectionType.Contact)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            ToastHelper.show(getContext(), getString(R.string.user_deleted));
                            getActivity().finish();
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                            Toast.makeText(ProfileFragment.this.getContext(), throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }));
                }
            });
        }


        // Country Flag
        String countryCode = getUser().getCountryCode();
        flagImageView.setVisibility(View.INVISIBLE);

        if(countryCode != null && !countryCode.isEmpty()) {
            int flagResourceId = getFlagResId(countryCode);
            if(flagResourceId >= 0) {
                flagImageView.setImageResource(flagResourceId);
                flagImageView.setVisibility(View.VISIBLE);
            }
        }

        // Profile Image
        avatarImageView.setImageURI(getUser().getAvatarURL());

        String status = getUser().getStatus();
        if(!StringChecker.isNullOrEmpty(status)) {
            statusTextView.setText(status);
        }
        else {
            statusTextView.setText("");
        }

        // Name
        nameTextView.setText(getUser().getName());

        String availability = getUser().getAvailability();

        // Availability
        if(availability != null) {
            availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(availability));
            availabilityImageView.setVisibility(View.VISIBLE);
        }
        else {
            availabilityImageView.setVisibility(View.INVISIBLE);
        }

        // Location
        locationTextView.setText(getUser().getLocation());

        // Phone
        phoneTextView.setText(getUser().getPhoneNumber());

        String presenceSubscription = getUser().getPresenceSubscription();

        boolean follows = false;
        boolean followed = false;
        if(presenceSubscription != null) {
            follows = presenceSubscription.equals("from") || presenceSubscription.equals("both");
            followed = presenceSubscription.equals("to") || presenceSubscription.equals("both");
        }

//        if(follows) {
//            followsImageView.setMaxHeight(followsHeight);
//            followsTextView.setMaxHeight(followsHeight);
//        }
//        else {
//            followsImageView.setMaxHeight(0);
//            followsTextView.setMaxHeight(0);
//        }
//        if(followed) {
//            followedImageView.setMaxHeight(followedHeight);
//            followedTextView.setMaxHeight(followedHeight);
//        }
//        else {
//            followedImageView.setMaxHeight(0);
//            followedTextView.setMaxHeight(0);
//        }


        ConstraintLayout layout = (ConstraintLayout) mainView.findViewById(R.id.mainConstraintLayout);
        ConstraintSet set = new ConstraintSet();
        set.clone(layout);

        ArrayList<Integer> imageViewIds = new ArrayList<>();
        imageViewIds.add(R.id.ivLocation);
        imageViewIds.add(R.id.ivPhone);
        imageViewIds.add(R.id.ivFollows);
        imageViewIds.add(R.id.ivFollowed);

        stackViews(imageViewIds, R.id.tvStatus, set);

        ArrayList<Integer> textViewIds = new ArrayList<>();
        textViewIds.add(R.id.tvLocation);
        textViewIds.add(R.id.tvPhone);
        textViewIds.add(R.id.tvFollows);
        textViewIds.add(R.id.tvFollowed);
        textViewIds.add(R.id.btnDelete);
        textViewIds.add(R.id.btnBlock);

        stackViews(textViewIds, R.id.tvStatus, set);

        set.applyTo(layout);
    }

    private void stackViews (ArrayList<Integer> viewIds, Integer firstViewId, ConstraintSet set) {
        int lastViewId = firstViewId;
        final float density = getContext().getResources().getDisplayMetrics().density;
        for(int viewId : viewIds) {
            View view = mainView.findViewById(viewId);
            if(view.getVisibility() == View.VISIBLE) {
                set.connect(viewId, ConstraintSet.TOP, lastViewId, ConstraintSet.BOTTOM, (int) (ProfileDetailMargin * density));
                //set.constrainHeight(viewId, ProfileDetailRowHeight * density);
                lastViewId = viewId;
            }
        }
    }

    private User getUser () {
        return user != null ? user : NM.currentUser();
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
            e.printStackTrace();
        }
        return -1;
    }

    public void showSettings() {
        InterfaceManager.shared().a.startEditProfileActivity(getContext(), NM.currentUser().getEntityID());
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

        for(Disposable d : disposables) {
            d.dispose();
        }
        disposables.clear();
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
