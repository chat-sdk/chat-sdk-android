package sdk.chat.firebase.location;

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
import io.reactivex.disposables.Disposable;
import sdk.chat.core.dao.User;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.PermissionRequestHandler;
import sdk.chat.ui.R2;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.fragments.BaseFragment;
import sdk.chat.ui.utils.ToastHelper;
import sdk.guru.common.RX;

/**
 * Created by Erk on 30.03.2016.
 */
public class NearbyUsersFragment extends BaseFragment {

    protected UsersListAdapter adapter;

    @BindView(R2.id.recyclerView)
    protected RecyclerView recyclerView;

    @BindView(R2.id.textView)
    protected TextView textView;

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

    }

    public void start() {
        FirebaseNearbyUsersModule.shared().getLocationHandler().start();
        reloadData();
//        textView.setVisibility(View.GONE);
//        recyclerView.setVisibility(View.VISIBLE);
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

        reloadData();


        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        dm.add(FirebaseNearbyUsersModule.shared().getGeoFireManager().locationUsersEvents().observeOn(RX.db()).subscribe(locationUsers -> {
            reloadData();
        }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> reloadData(), throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .subscribe(networkEvent -> reloadData(), throwable -> ToastHelper.show(getContext(), throwable.getLocalizedMessage())));

    }

    @Override
    public void onStop() {
        super.onStop();

        dm.dispose();
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
            reloadData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePermissions();
        reloadData();
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

        if (!FirebaseNearbyUsersModule.shared().config.enabled) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.nearby_users_disabled);
            recyclerView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter != null) {

                // Build a list of location Users
                ArrayList<LocationUser> users = new ArrayList<>();

                for (LocationUser lu : FirebaseNearbyUsersModule.shared().getGeoFireManager().getLocationUsers()) {
                    if (lu.distanceToMe() < FirebaseNearbyUsersModule.config().maxDistance && lu.getIsOnline()) {
                        users.add(lu);
                    }
                }

                Collections.sort(users, (o1, o2) -> (int) Math.round(o1.distanceToMe() - o2.distanceToMe()));

                ArrayList<UserListItem> items = new ArrayList<>(users);

                RX.main().scheduleDirect(() -> {
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
                });
            }
        }
    }

}
