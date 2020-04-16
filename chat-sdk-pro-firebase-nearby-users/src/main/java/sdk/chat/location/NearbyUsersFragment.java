package sdk.chat.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import co.chatsdk.firebase.nearby_users.R;
import co.chatsdk.firebase.nearby_users.R2;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.adapters.UsersListAdapter;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.disposables.Disposable;
import sdk.chat.core.utils.PermissionRequestHandler;

/**
 * Created by Erk on 30.03.2016.
 */
public class NearbyUsersFragment extends BaseFragment {

    protected UsersListAdapter adapter;

    @BindView(R2.id.recyclerView)
    protected RecyclerView recyclerView;

    @BindView(R2.id.textView)
    protected TextView textView;

    protected ArrayList<LocationUser> locationUsers = new ArrayList<>();

    protected Disposable listOnClickListenerDisposable;

    public static NearbyUsersFragment newInstance() {
        NearbyUsersFragment f = new NearbyUsersFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_nearby_users;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GeoFireManager.shared().allEvents().doOnNext(geoEvent -> {
            if (geoEvent.item.isType(GeoItem.USER)) {
                String entityID = geoEvent.item.entityID;
                User user = ChatSDK.core().getUserNowForEntityID(entityID);
                if (!user.isMe()) {
                    LocationUser lu = getLocationUser(entityID);

                    if (geoEvent.type.equals(GeoEvent.Type.Entered) && lu == null) {
                        locationUsers.add(new LocationUser(user, geoEvent.getLocation()));
                    }
                    if (geoEvent.type.equals(GeoEvent.Type.Exited) && lu != null) {
                        locationUsers.remove(lu);
                    }
                    if (geoEvent.type.equals(GeoEvent.Type.Moved) && lu != null) {
                        lu.location = geoEvent.getLocation();
                    }
                    reloadData();
                }
            }
        }).doOnError(this).ignoreElements().subscribe(this);

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> reloadData(), throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .subscribe(networkEvent -> reloadData(), throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));
    }

    public void start() {
        LocationHandler.shared().start();
        textView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void permissionError(Throwable t) {
        textView.setVisibility(View.VISIBLE);
        textView.setText(t.getLocalizedMessage());
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        adapter = new UsersListAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

//        this.reloadData();

        return view;
    }

    @Override
    protected void initViews() {

    }

    @Override
    public void clearData() {

    }

    public void setTabVisibility (boolean isVisible) {
        if (isVisible) {
            updatePermissions();
//            reloadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissions();
    }

    public void updatePermissions() {
        if (getActivity() != null) {
            PermissionRequestHandler
                    .requestLocationAccess(getActivity()).doOnComplete(this::start)
                    .doOnError(this::permissionError).subscribe(this);
        }
    }

    @Override
    public void reloadData() {
        if (adapter != null) {

            // Build a list of location Users
            ArrayList<LocationUser> users = new ArrayList<>();

            for (LocationUser lu : locationUsers) {
                if (lu.distanceToMe() < FirebaseNearbyUsersModule.config().maxDistance && lu.getIsOnline()) {
                    users.add(lu);
                }
            }

            Collections.sort(users, (o1, o2) -> (int) Math.round(o1.distanceToMe() - o2.distanceToMe()));

            ArrayList<UserListItem> items = new ArrayList<>(users);

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
