package co.chatsdk.ui.profile;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Locale;

import co.chatsdk.core.NM;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.defines.Availability;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.core.utils.StringUtils;
import co.chatsdk.ui.R;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.helpers.UIHelper;
import co.chatsdk.ui.utils.AvailabilityHelper;
import co.chatsdk.ui.utils.UserAvatarHelper;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 8/15/17.
 */

public class ProfileFragment extends BaseFragment {

    private CircleImageView avatarImageView;
    private ImageView flagImageView;
    private ImageView availabilityImageView;
    private TextView nameTextView;
    private TextView statusTextView;
    private TextView locationTextView;
    private TextView phoneTextView;
    private TextView dateOfBirthTextView;
    private TextView followsTextView;
    private TextView followedTextView;
    private Button blockButton;
    private Button deleteButton;

    private ImageView locationImageView;
    private ImageView phoneImageView;
    private ImageView dateOfBirthImageView;
    private ImageView followsImageView;
    private ImageView followedImageView;

    private ArrayList<Disposable> disposables = new ArrayList<>();

    int followsHeight;
    int followedHeight;

    public User user;

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

        disposables.add(NM.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.UserMetaUpdated)).subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                if(networkEvent.user.equals(user)) {
                    loadData();
                }
            }
        }));

        initViews(inflater);
        loadData();


        return mainView;
    }

    public void initViews(LayoutInflater inflater){
        mainView = inflater.inflate(R.layout.chat_sdk_profile_fragment, null);

        setupTouchUIToDismissKeyboard(mainView, R.id.ivAvatar);

        avatarImageView = (CircleImageView) mainView.findViewById(R.id.ivAvatar);
        flagImageView = (ImageView) mainView.findViewById(R.id.ivFlag);
        availabilityImageView = (ImageView) mainView.findViewById(R.id.ivAvailability);
        nameTextView = (TextView) mainView.findViewById(R.id.tvName);
        statusTextView = (TextView) mainView.findViewById(R.id.tvStatus);

        locationTextView = (TextView) mainView.findViewById(R.id.tvLocation);
        phoneTextView = (TextView) mainView.findViewById(R.id.tvPhone);
        dateOfBirthTextView = (TextView) mainView.findViewById(R.id.tvDateOfBirth);
        followsTextView = (TextView) mainView.findViewById(R.id.tvFollows);
        followedTextView = (TextView) mainView.findViewById(R.id.tvFollowed);
        blockButton = (Button) mainView.findViewById(R.id.btnBlock);
        deleteButton = (Button) mainView.findViewById(R.id.btnDelete);

        followsHeight = followsTextView.getHeight();
        followedHeight = followedTextView.getHeight();

        locationImageView = (ImageView) mainView.findViewById(R.id.ivLocation);
        phoneImageView = (ImageView) mainView.findViewById(R.id.ivPhone);
        dateOfBirthImageView = (ImageView) mainView.findViewById(R.id.ivDateOfBirth);
        followsImageView = (ImageView) mainView.findViewById(R.id.ivFollows);
        followedImageView = (ImageView) mainView.findViewById(R.id.ivFollowed);

        setUser(user);

        disposables.add(NM.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .subscribe(new Consumer<NetworkEvent>() {
            @Override
            public void accept(@NonNull NetworkEvent networkEvent) throws Exception {
                if(networkEvent.user.equals(user)) {
                    setUser(user);
                }
            }
        }));
    }

    private void updateBlockedButton (boolean blocked) {
        if (blocked) {
            blockButton.setText(getString(R.string.unblock));
        }
        else {
            blockButton.setText(getString(R.string.block));
        }
    }

    public void setUser (User user) {
        if(user == null) {
            return;
        }
        this.user = user;

        boolean isCurrentUser = NM.currentUser().equals(user);
        setHasOptionsMenu(isCurrentUser);

        int visibility = isCurrentUser ? View.INVISIBLE : View.VISIBLE;

        followsImageView.setVisibility(visibility);
        followedImageView.setVisibility(visibility);
        followsTextView.setVisibility(visibility);
        followedTextView.setVisibility(visibility);
        blockButton.setVisibility(visibility);
        deleteButton.setVisibility(visibility);

        if (!isCurrentUser) {
            // Find out if the user is blocked already?
            disposables.add(NM.blocking().isBlocked(user).subscribe(new BiConsumer<Boolean, Throwable>() {
                @Override
                public void accept(Boolean blocked, Throwable throwable) throws Exception {
                    updateBlockedButton(blocked);
                }
            }));

            blockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disposables.add(NM.blocking().isBlocked(ProfileFragment.this.user).subscribe(new BiConsumer<Boolean, Throwable>() {
                        @Override
                        public void accept(Boolean blocked, Throwable throwable) throws Exception {
                            if(blocked) {
                                NM.blocking().unblockUser(ProfileFragment.this.user).subscribe(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        updateBlockedButton(false);
                                        ProfileFragment.this.showToast(getString(R.string.user_unblocked));
                                    }
                                });
                            }
                            else {
                                NM.blocking().blockUser(ProfileFragment.this.user).subscribe(new Action() {
                                    @Override
                                    public void run() throws Exception {
                                        updateBlockedButton(true);
                                        ProfileFragment.this.showToast(getString(R.string.user_blocked));
                                    }
                                });
                            }
                        }
                    }));
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    disposables.add(NM.contact().deleteContact(ProfileFragment.this.user, ConnectionType.Contact).subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            ProfileFragment.this.showToast(getString(R.string.user_deleted));
                            getActivity().finish();
                        }/**/
                    }));
                }
            });
        }

        this.updateInterface();
    }

    private void updateInterface () {

        // Country Flag
        String countryCode = user.getCountryCode();
        flagImageView.setVisibility(View.INVISIBLE);

        if(countryCode != null && !countryCode.isEmpty()) {
            int flagResourceId = getFlagResId(countryCode);
            if(flagResourceId >= 0) {
                flagImageView.setImageResource(flagResourceId);
                flagImageView.setVisibility(View.VISIBLE);
            }
        }

        // Profile Image
        UserAvatarHelper.loadAvatar(user, avatarImageView).subscribe();

        String status = user.getStatus();
        if(!StringUtils.isNullOrEmpty(status)) {
            statusTextView.setText(status);
        }
        else {
            statusTextView.setText("");
        }

        // Name
        nameTextView.setText(user.getName());

        String availability = user.getAvailability();

        // Availability
        if(availability != null) {
            availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(availability));
            availabilityImageView.setVisibility(View.VISIBLE);
        }
        else {
            availabilityImageView.setVisibility(View.INVISIBLE);
        }

        // Location
        locationTextView.setText(user.getLocation());

        // Phone
        phoneTextView.setText(user.getPhoneNumber());

        // Date of birth
        dateOfBirthTextView.setText(user.getDateOfBirth());

        String presenceSubscription = user.getPresenceSubscription();

        boolean follows = false;
        boolean followed = false;
        if(presenceSubscription != null) {
            follows = presenceSubscription.equals("from") || presenceSubscription.equals("both");
            followed = presenceSubscription.equals("to") || presenceSubscription.equals("both");
        }

        if(follows) {
            followsImageView.setMaxHeight(followsHeight);
            followsTextView.setMaxHeight(followsHeight);
        }
        else {
            followsImageView.setMaxHeight(0);
            followsTextView.setMaxHeight(0);
        }
        if(followed) {
            followedImageView.setMaxHeight(followedHeight);
            followedTextView.setMaxHeight(followedHeight);
        }
        else {
            followedImageView.setMaxHeight(0);
            followedTextView.setMaxHeight(0);
        }

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
        // Logout and return to the login activity.
//        FacebookManager.logout(getActivity());
//
//        NM.auth().logout();
//        uiHelper.startLoginActivity(true);
        UIHelper.shared().startEditProfileActivity(false, NM.currentUser());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (!user.equals(NM.currentUser()))
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

}
