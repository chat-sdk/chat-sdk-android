package co.chatsdk.core.base;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.HandlerThread;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import androidx.core.content.ContextCompat;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.PermissionRequestHandler;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

/**
 * Created by Pepe on 01/25/19.
 */

public class BaseLocationAdapter {

    protected final FusedLocationProviderClient locationClient;

    private Disposable locationDisposable;

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
        return Observable.create(observable -> {
            if (ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                observable.onError(new Error("location permissions not granted"));
                return;
            }
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(interval * 1000);
            locationRequest.setFastestInterval(interval * 1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationDisposable != null) {
                        locationDisposable.dispose();
                    }
                    locationDisposable = getMostAccurateLocation(locationResult.getLocations()).subscribe(observable::onNext);
                }
            };
            HandlerThread locationUpdatesThread = new HandlerThread("LocationUpdatesThread");
            locationUpdatesThread.start();
            Looper locationUpdatesLooper = locationUpdatesThread.getLooper();
            locationClient.requestLocationUpdates(locationRequest, locationCallback, locationUpdatesLooper);
        });
    }

    public Single<Location> getLastLocation() {
        return Single.create(single -> {
            if (ContextCompat.checkSelfPermission(context(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                single.onError(new Error("location permissions not granted"));
                return;
            }
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

    public Single<Location> getMostAccurateLocation(List<Location> locations) {
        return Single.create(single -> {
            Location accurateLocation = null;
            for (Location location : locations) {
                if (location == null) continue;
                if (accurateLocation == null || location.getAccuracy() >= accurateLocation.getAccuracy()) {
                    accurateLocation = location;
                }
            }
            if (accurateLocation != null) {
                single.onSuccess(accurateLocation);
            } else {
                single.onError(new Error("no accurate location found"));
            }
        });
    }

}