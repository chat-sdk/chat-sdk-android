package co.chatsdk.firebase.nearby_users;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.chatsdk.core.base.BaseHookHandler;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.EventType;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.main.BaseFragment;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Created by Erk on 30.03.2016.
 */
public class NearbyUsersFragment extends BaseFragment implements GeoFireManagerDelegate {

    private FusedLocationProviderClient locationClient;
    private boolean permissionsGranted;
    private Location currentLocation;

    protected NearbyUsersListAdapter adapter;
    protected ProgressBar progressBar;
    protected RecyclerView recyclerView;

    protected GeoFireManager geoFire;

    protected Disposable listOnClickListenerDisposable;

    protected ArrayList<LocationUser> locationUsers = new ArrayList<>();
    protected DisposableList disposables = new DisposableList();

    public static NearbyUsersFragment newInstance() {
        NearbyUsersFragment f = new NearbyUsersFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLocationClient();

        geoFire = new GeoFireManager(this);

        disposables.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .subscribe(networkEvent -> reloadData()));

    }

    public void startUpdatingLocation () {
        updateLocation();
        Observable.interval(10, TimeUnit.SECONDS).subscribe(aLong -> {
            updateLocation();
        });
        ChatSDK.hook().addHook(new Hook(data -> {
            updateLocation();
        }), BaseHookHandler.SetUserOnline);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainView = inflater.inflate(R.layout.chat_sdk_firebase_nearby_users, null);

        recyclerView = mainView.findViewById(R.id.list_nearby_users);
        progressBar = mainView.findViewById(co.chatsdk.ui.R.id.chat_sdk_progressbar);

        adapter = new NearbyUsersListAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        this.reloadData();


        return mainView;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 314) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = true;
                updateLocation();
            }
        }
    }

    private void initLocationClient() {
        locationClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        requestPermissions();
    }

    private void requestPermissions() {
        if (this.getContext() != null) {
            if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                this.requestPermissions(permissions, 314);
                permissionsGranted = false;
            } else {
                permissionsGranted = true;
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        if (permissionsGranted && this.getActivity() != null) {
            locationClient.getLastLocation().addOnSuccessListener(this.getActivity(), location -> {
                if (location != null) {
                    currentLocation = location;

                    geoFire.setLocation(location.getLatitude(), location.getLongitude());

                }
            });
        } else {
            requestPermissions();
        }
    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {
        // Build a list of location Users
        ArrayList<LocationUser> users = new ArrayList<>();

        for (LocationUser lu : locationUsers) {
            lu.referenceLocation = new GeoLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
            if (lu.distanceToReference() < GeoFireManager.GeoLocationRadius * 1000) {
                users.add(lu);
            }
        }

        Collections.sort(users, (o1, o2) -> (int) Math.round(o1.distanceToReference() - o2.distanceToReference()));

        ArrayList<UserListItem> items = new ArrayList<>();
        for (LocationUser lu : users) {
            items.add(lu);
        }

        adapter.setUsers(items);
        if (listOnClickListenerDisposable != null) {
            listOnClickListenerDisposable.dispose();
        }
        listOnClickListenerDisposable = adapter.getItemClicks().subscribe(o -> {
            if (o instanceof LocationUser) {
                final User clickedUser = ((LocationUser) o).getUser();
                ChatSDK.ui().startProfileActivity(getContext(), clickedUser.getEntityID());
            }
        });
    }

    @Override
    public void userAdded(User user, GeoLocation location) {
        if (getLocationUser(user.getEntityID()) == null) {
            locationUsers.add(new LocationUser(user, location));
            reloadData();
        }
    }

    @Override
    public void userRemoved(User user) {
        LocationUser lu = getLocationUser(user.getEntityID());
        if (lu != null) {
            locationUsers.remove(lu);
            reloadData();
        }
    }

    @Override
    public void userMoved(User user, GeoLocation location) {
        LocationUser lu = getLocationUser(user.getEntityID());
        if (lu != null) {
            lu.setLocation(location);
            reloadData();
        }
    }

    public LocationUser getLocationUser(String entityID) {
        for (LocationUser lu : locationUsers) {
            if (lu.getUser().getEntityID().equals(entityID)) {
                return lu;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        startUpdatingLocation();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.dispose();
    }

}
