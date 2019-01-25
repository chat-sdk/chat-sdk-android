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
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.PermissionRequestHandler;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Pepe on 01/25/19.
 */

public class BaseLocationAdapter {

    protected final FusedLocationProviderClient locationClient;
    protected final DisposableList disposableList = new DisposableList();

    protected Context context() {
        return ChatSDK.shared().context();
    }

    public BaseLocationAdapter() {
        locationClient = LocationServices.getFusedLocationProviderClient(context());
    }

    public Observable<Location> requestLocationUpdates(long interval, int distance) {
        return requestLocationUpdates(interval)
                .distinctUntilChanged((l1, l2) -> l1.distanceTo(l2) < distance);
    }

    public Observable<Location> requestLocationUpdates(long interval) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(interval * 1000);
        locationRequest.setFastestInterval(interval * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Observable.error(new Error("location permissions not granted"));
        }
        return Observable.create(observable -> {
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
        if (ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return Single.error(new Error("location permissions not granted"));
        }
        return Single.create(single -> {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    single.onSuccess(location);
                } else {
                    single.onError(new Error("location is null"));
                }
            }).addOnFailureListener(single::onError);
        });
    }

    public Completable requestPermissions(final Activity activity) {
        return PermissionRequestHandler.shared().requestLocationAccess(activity);
    }

    public Location getMostAccurateLocation(List<Location> locations) {
        Location accurrateLocation = null;
        for (Location location : locations) {
            if (location == null) continue;
            if (accurrateLocation == null || location.getAccuracy() >= accurrateLocation.getAccuracy()) {
                accurrateLocation = location;
            }
        }
        return accurrateLocation;
    }

    public void dispose() {
        disposableList.dispose();
    }

}