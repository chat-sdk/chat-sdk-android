package co.chatsdk.firebase.nearby_users;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.AppBackgroundMonitor;
import sdk.chat.core.utils.PermissionRequestHandler;

public class LocationHandler {

    protected static final LocationHandler instance = new LocationHandler();
    protected FusedLocationProviderClient client;

    protected Context context;
    protected Location location = null;

    protected List<SingleEmitter<Location>> singleEmitters = new ArrayList<>();

    protected PublishSubject<Location> onLocationUpdate = PublishSubject.create();
    protected PublishSubject<Location> onLocationUpdateWhenMinDistance = PublishSubject.create();


    Disposable timerDisposable;

    public static LocationHandler shared() {
        return instance;
    }

    public LocationHandler() {
        AppBackgroundMonitor.shared().addListener(new AppBackgroundMonitor.Listener() {
            @Override
            public void didStart() {
                start();
            }

            @Override
            public void didStop() {
                stop();
            }
        });

        ChatSDK.hook().addHook(Hook.sync(data -> {
            start();
        }), HookEvent.DidAuthenticate);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            stop();
        }), HookEvent.WillLogout);

    }

    public void initialize(Context context) {
        this.context = context;
        this.client = new FusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    public void start() {
        if (timerDisposable == null) {
            if (PermissionRequestHandler.permissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                stop();
                timerDisposable = Observable.interval(FirebaseNearbyUsersModule.config().minRefreshTime, TimeUnit.SECONDS).subscribe(aLong -> {
                    client.getLastLocation().addOnSuccessListener(location -> {
                        updateLocation(location);
                    });
                });
            }
        }
    }

    public void stop() {
        if (timerDisposable != null) {
            timerDisposable.dispose();
        }
    }

    protected void updateLocation(Location newLocation) {
        if (location == null || location.distanceTo(newLocation) > FirebaseNearbyUsersModule.config().minRefreshDistance) {
            location = newLocation;
            onLocationUpdateWhenMinDistance.onNext(location);
        }
        onLocationUpdate.onNext(location);
        for (SingleEmitter<Location> emitter: singleEmitters) {
            emitter.onSuccess(location);
        }
        singleEmitters.clear();
    }

    public Observable<Location> getLocationUpdate() {
        return onLocationUpdate.hide();
    }

    public Observable<Location> getLocationUpdateWhenMinDistance() {
        return onLocationUpdateWhenMinDistance.hide();
    }

    public Single<Location> once() {
        return Single.create(emitter -> {
            if (location != null) {
                emitter.onSuccess(location);
            } else {
                singleEmitters.add(emitter);
            }
        });
    }

    public Location getLocation() {
        return location;
    }
}
