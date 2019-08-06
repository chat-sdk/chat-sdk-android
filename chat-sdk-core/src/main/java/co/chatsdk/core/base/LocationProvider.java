package co.chatsdk.core.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;

import java.util.List;

import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.core.utils.PermissionRequestHandler;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Pepe on 01/25/19.
 */

public class LocationProvider {

    protected final FusedLocationProviderClient locationClient;
    protected final LocationRequest locationUpdatesRequest;
    protected final SettingsClient settingsClient;
    protected final DisposableList disposableList = new DisposableList();

    protected LocationCallback locationCallback;

    protected Context context() {
        return ChatSDK.shared().context();
    }

    protected final int ENABLE_LOCATION_SERVICES_REQUEST = 32;

    public LocationProvider() {
        locationClient = LocationServices.getFusedLocationProviderClient(context());
        locationUpdatesRequest = new LocationRequest();
        locationUpdatesRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        settingsClient = LocationServices.getSettingsClient(context());
    }

    public Completable requestEnableLocationServices(Activity activity) {
        return Completable.create(e -> {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(locationUpdatesRequest);
            settingsClient.checkLocationSettings(builder.build())
            .addOnCompleteListener(task -> {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    if (response == null) {
                        e.onError(new Error(context().getString(R.string.location_services_problem_message)));
                        return;
                    }
                    LocationSettingsStates states = response.getLocationSettingsStates();
                    if (states == null) {
                        e.onError(new Error(context().getString(R.string.location_services_problem_message)));
                        return;
                    }
                    if (states.isLocationUsable()) {
                        e.onComplete();
                    } else {
                        e.onError(new Error(context().getString(R.string.location_services_not_enabled)));
                    }
                } catch (ApiException exception) {
                    if (exception.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            ResolvableApiException apiException = (ResolvableApiException) exception;
                            apiException.startResolutionForResult(activity, ENABLE_LOCATION_SERVICES_REQUEST);
                            disposableList.add(ActivityResultPushSubjectHolder.shared()
                                    .filter(activityResult -> activityResult.requestCode == ENABLE_LOCATION_SERVICES_REQUEST)
                                    .subscribe(activityResult -> {
                                        if (activityResult.resultCode == Activity.RESULT_OK) {
                                            e.onComplete();
                                        } else {
                                            e.onError(new Error(context().getString(R.string.location_services_not_enabled)));
                                        }
                                    }));
                        } catch (IntentSender.SendIntentException exeption) {
                            e.onError(exception);
                        }
                    } else {
                         e.onError(exception);
                    }
                }
            });
        });
    }

    public Observable<Location> requestLocationUpdates(Activity activity, long interval, int distance) {
        return requestLocationUpdates(activity, interval)
                .distinctUntilChanged((l1, l2) -> l1.distanceTo(l2) < distance);
    }

    @SuppressLint("MissingPermission")
    public Observable<Location> requestLocationUpdates(Activity activity, long interval) {
        return requestEnableLocationServices(activity)
                .andThen(PermissionRequestHandler.shared().requestLocationAccess(activity))
                .andThen(Observable.create((ObservableOnSubscribe<Location>) observable -> {
                    locationUpdatesRequest.setInterval(interval * 1000);
                    locationUpdatesRequest.setFastestInterval(interval * 1000);

                    if (locationCallback != null) {
                        locationClient.removeLocationUpdates(locationCallback);
                    }
                    locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            Location location = getMostAccurateLocation(locationResult.getLocations());
                            if (location != null) {
                                observable.onNext(location);
                            }
                        }
                    };

                    activity.runOnUiThread(() -> {
                        locationClient.requestLocationUpdates(locationUpdatesRequest, locationCallback, Looper.myLooper());
                    });
                }).subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread()));
    }

    @SuppressLint("MissingPermission")
    public Single<Location> getLastLocation(Activity activity) {
        return PermissionRequestHandler.shared().requestLocationAccess(activity)
                .andThen((Single.create((SingleOnSubscribe<Location>) single -> {
                    locationClient.getLastLocation().addOnSuccessListener(location -> {
                        if (location != null) {
                            single.onSuccess(location);
                        } else {
                            single.onError(new Error(context().getResources().getString(R.string.location_is_null)));
                        }
                    }).addOnFailureListener(single::onError);
                })).subscribeOn(Schedulers.single()));
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
