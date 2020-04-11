package co.chatsdk.firebase.nearby_users;

import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoLocation;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.adapters.UsersListAdapter;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by Erk on 30.03.2016.
 */
public class NearbyUsersFragment extends BaseFragment {

    protected UsersListAdapter adapter;

    @BindView(R2.id.progressBar)
    protected ProgressBar progressBar;

    @BindView(R2.id.recyclerView)
    protected RecyclerView recyclerView;

    protected LocationUpdater updater;

    protected Disposable listOnClickListenerDisposable;

    protected ArrayList<LocationUser> locationUsers = new ArrayList<>();

    public static NearbyUsersFragment newInstance() {
        NearbyUsersFragment f = new NearbyUsersFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dm.add(GeoFireManager.shared().allEvents().observeOn(AndroidSchedulers.mainThread()).flatMapCompletable(geoEvent -> {
            if (geoEvent.item.isType(GeoItem.USER)) {
                String entityID = geoEvent.item.entityID;
                User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, entityID);
                if (!user.isMe()) {
                    LocationUser lu = getLocationUser(entityID);

                    if (geoEvent.type.equals(GeoEvent.Type.Entered) && lu == null) {
                        locationUsers.add(new LocationUser(user, geoEvent.location));
                    }
                    if (geoEvent.type.equals(GeoEvent.Type.Exited) && lu != null) {
                        locationUsers.remove(lu);
                    }
                    if (geoEvent.type.equals(GeoEvent.Type.Moved) && lu != null) {
                        lu.location = geoEvent.location;
                    }
                }
                return ChatSDK.core().userOn(user);
            }
            return Completable.complete();
        }).subscribe(this::reloadData, throwable -> {
            throwable.printStackTrace();
            if (Looper.myLooper() != null) {
                ToastHelper.show(getContext(), throwable.getLocalizedMessage());
            }
        }));

        updater = new LocationUpdater(getActivity(), currentUserItem());

        ChatSDK.hook().addHook(Hook.sync(data -> {
            GeoFireManager.shared().removeItem(currentUserItem());
        }), HookEvent.UserWillDisconnect);

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> reloadData(), throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .subscribe(networkEvent -> reloadData(), throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_nearby_users, null);

        adapter = new UsersListAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        this.reloadData();

        return rootView;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_nearby_users;
    }

    @Override
    protected void initViews() {

    }

    private GeoItem currentUserItem () {
        return new GeoItem(ChatSDK.currentUser().getEntityID(), GeoItem.USER);
    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {
        // Build a list of location Users
        ArrayList<LocationUser> users = new ArrayList<>();

        if (updater.currentLocation() != null) {
            for (LocationUser lu : locationUsers) {
                lu.referenceLocation = new GeoLocation(updater.currentLocation().getLatitude(), updater.currentLocation().getLongitude());
                if (lu.distanceToReference() < FirebaseNearbyUsersModule.config().maxDistance) {
                    users.add(lu);
                }
            }
        }

        Collections.sort(users, (o1, o2) -> (int) Math.round(o1.distanceToReference() - o2.distanceToReference()));

        ArrayList<UserListItem> items = new ArrayList<>();
        for (LocationUser lu : users) {
            items.add(lu);
        }

        adapter.setUsers(items);
        if(listOnClickListenerDisposable != null) {
            listOnClickListenerDisposable.dispose();
        }
        listOnClickListenerDisposable = adapter.onClickObservable().subscribe(o -> {
            if(o instanceof LocationUser) {
                final User clickedUser = ((LocationUser) o).user;
                ChatSDK.ui().startProfileActivity(getContext(), clickedUser.getEntityID());
            }
        });
    }

    public LocationUser getLocationUser(String entityID) {
        for (LocationUser lu : locationUsers) {
            if (lu.user.getEntityID().equals(entityID)) {
                return lu;
            }
        }
        return null;
    }
}
