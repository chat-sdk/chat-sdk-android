package co.chatsdk.firebase.nearby_users;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.location.Location;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.core.utils.PermissionRequestHandler;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class LocationUpdater {

    protected Disposable listUpdateDisposable;
    protected ArrayList<GeoItem> items = new ArrayList<>();
    protected boolean permissionsGranted;
    protected WeakReference<Activity> activity;

    protected LocationFinder locationFinder;

    public LocationUpdater (Activity activity, GeoItem... items) {

        locationFinder = new LocationFinder(activity);

        this.activity = new WeakReference<>(activity);
        this.items.addAll(Arrays.asList(items));

        initLocationClient();

        AppBackgroundMonitor.shared().addListener(new AppBackgroundMonitor.Listener() {
            @Override
            public void didStart() {
                startUpdatingLocation();
            }

            @Override
            public void didStop() {
                stopUpdatingLocation();
            }
        });

        ChatSDK.hook().addHook(Hook.sync(data -> {
            startUpdatingLocation();
        }), HookEvent.UserDidConnect);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            stopUpdatingLocation();
        }), HookEvent.DidLogout);

        startUpdatingLocation();
    }

    private void initLocationClient() {
        if (activity != null) {
            requestPermissions();
        }
    }

    public void startUpdatingLocation () {
        updateLocation();

        if (listUpdateDisposable != null) {
            listUpdateDisposable.dispose();
        }

        listUpdateDisposable = Observable.interval(10, TimeUnit.SECONDS).subscribe(aLong -> {
            updateLocation();
        });
    }

    public void stopUpdatingLocation () {
        if (listUpdateDisposable != null) {
            listUpdateDisposable.dispose();
        }
    }

    private void requestPermissions() {
        Disposable d = PermissionRequestHandler.requestLocationAccess(activity.get()).subscribe(() -> {
            permissionsGranted = true;
            updateLocation();
        }, throwable -> permissionsGranted = false);
    }

    @SuppressLint("MissingPermission")
    private void updateLocation() {
        if (permissionsGranted) {
            Disposable d = locationFinder.updateLocation().subscribe(location -> {
                if (GeoFireManager.shared().updateLocation(location.getLatitude(), location.getLongitude())) {
                    for (GeoItem item : items) {
                        GeoFireManager.shared().addItemAtCurrentLocation(item);
                    }
                }
            }, throwable -> throwable.printStackTrace());
        } else {
            requestPermissions();
        }
    }

    public Location currentLocation () {
        return locationFinder.currentLocation;
    }
}
