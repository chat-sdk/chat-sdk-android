package co.chatsdk.core.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.PermissionRequestHandler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

    public Observable<Location> requestLocationUpdates(Activity activity, long interval, int distance) {
        return requestLocationUpdates(interval)
                .distinctUntilChanged((l1, l2) -> l1.distanceTo(l2) < distance);
    }

    public Observable<Location> requestLocationUpdates(Activity activity, long interval) {
        return PermissionRequestHandler.shared().requestLocationAccess(activity)
                .andThen(requestLocationUpdates(interval));
    }

    public Single<Location> getLastLocation(Activity activity) {
        return PermissionRequestHandler.shared().requestLocationAccess(activity)
                .andThen(getLastLocation());
    }

     @SuppressLint("MissingPermission")
    protected Observable<Location> requestLocationUpdates(long interval) {
        return Observable.create((ObservableOnSubscribe<Location>) observable -> {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(interval * 1000);
            locationRequest.setFastestInterval(interval * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
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
        }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    @SuppressLint("MissingPermission")
    protected Single<Location> getLastLocation() {
        return Single.create((SingleOnSubscribe<Location>) single -> {
                locationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        single.onSuccess(location);
                    } else {
                        single.onError(new Error(context().getResources().getString(R.string.location_is_null)));
                    }
                }).addOnFailureListener(single::onError);
        }).subscribeOn(Schedulers.single());
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
