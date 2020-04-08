package co.chatsdk.firebase.nearby_users;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.jetbrains.annotations.NotNull;

import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

public class LocationFinder {

    protected FusedLocationProviderClient locationClient;
    public Location currentLocation;
    public PublishSubject<Location> locationPublishSubject = PublishSubject.create();

    public LocationFinder (@NotNull Context context) {
        locationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public Single<Location> updateLocation () {
        return Single.create(emitter -> {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = location;
                    locationPublishSubject.onNext(location);
                    emitter.onSuccess(location);
                } else {
                     emitter.onError(new Throwable("Location null"));
                }
            });
        });
    }

    public Single<Location> getCurrentLocation () {
        return updateLocation();
//        return Single.create(emitter -> {
//            if (currentLocation != null) {
//                emitter.onSuccess(currentLocation);
//            } else {
//                updateLocation().subscribe(emitter::onSuccess);
//            }
//        });
    }

}
