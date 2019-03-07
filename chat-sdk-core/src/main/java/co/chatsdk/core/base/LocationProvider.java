package co.chatsdk.core.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import androidx.core.content.ContextCompat;
import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.PermissionRequestHandler;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Pepe on 01/25/19.
 */

public class LocationProvider {

    protected final FusedLocationProviderClient locationClient;
    protected final DisposableList disposableList = new DisposableList();

    protected Context context() {
        return ChatSDK.shared().context();
    }

    public LocationProvider() {
        locationClient = LocationServices.getFusedLocationProviderClient(context());
    }

    public Observable<Location> requestLocationUpdates(long interval, int distance) {
        return requestLocationUpdates(interval)
                .distinctUntilChanged((l1, l2) -> l1.distanceTo(l2) < distance);
    }

    public Observable<Location> requestLocationUpdates(long interval) {
        return Observable.create(observable -> {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(interval * 1000);
            locationRequest.setFastestInterval(interval * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            if (ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                observable.onError(new Error(context().getResources().getString(R.string.permission_location_not_granted)));
                return;
            }
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    Location location = getMostAccurateLocation(locationResult.getLocations());
                    if (location != null) {
                        observable.onNext(location);
                    }
                }
            };
            locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        });
    }

    public Single<Location> getLastLocation() {
        return Single.create(single -> {
            if (ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                single.onError(new Error(context().getResources().getString(R.string.permission_location_not_granted)));
                return;
            }
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    single.onSuccess(location);
                } else {
                    single.onError(new Error(context().getResources().getString(R.string.location_is_null)));
                }
            }).addOnFailureListener(single::onError);
        });
    }

    public Single<Location> getLastLocation(Activity activity) {
        return PermissionRequestHandler.shared().requestLocationAccess(activity)
                .andThen(getLastLocation());
    }

    public Location getMostAccurateLocation(List<Location> locations) {
        Location accurateLocation = null;
        for (Location location : locations) {
            if (location == null) continue;
            if (accurateLocation == null || location.getAccuracy() >= accurateLocation.getAccuracy()) {
                accurateLocation = location;
            }
        }
        return accurateLocation;
    }

    public void dispose() {
        disposableList.dispose();
    }

}