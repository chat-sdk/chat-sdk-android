package co.chatsdk.firebase.nearby_users;

import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.pmw.tinylog.Logger;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.hook.Hook;
import co.chatsdk.core.hook.HookEvent;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.firebase.FirebasePaths;
import co.chatsdk.firebase.wrappers.UserWrapper;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by ben on 4/3/18.
 */

public class GeoFireManager {

    protected GeoLocation location;
    protected GeoQuery query;

    protected PublishSubject<GeoEvent> eventPublishSubject = PublishSubject.create();
    protected ReplaySubject<GeoEvent> eventReplaySubject = ReplaySubject.create();

    protected static GeoFireManager shared = new GeoFireManager();

    public static GeoFireManager shared () {
        return shared;
    }

    public GeoFireManager () {

        ChatSDK.hook().addHook(Hook.sync(data -> {
            location = null;
            query.removeAllListeners();
        }), HookEvent.DidLogout);

        ChatSDK.hook().addHook(Hook.sync(data -> {
            location = null;
        }), HookEvent.UserWillDisconnect);

    }

    public void startListeningForItems(double latitude, double longitude, float radius) {

        stopListeningForItems();

        GeoFire geoFire = geoFireRef();
        GeoLocation center = new GeoLocation(latitude, longitude);
        query = geoFire.queryAtLocation(center, radius);

        GeoQueryEventListener listener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String id, GeoLocation location) {
                onNext(new GeoEvent(new GeoItem(id), location, GeoEvent.Type.Entered));
            }

            @Override
            public void onKeyExited(String id) {
                onNext(new GeoEvent(new GeoItem(id), location, GeoEvent.Type.Exited));
            }

            @Override
            public void onKeyMoved(String id, GeoLocation location) {
                onNext(new GeoEvent(new GeoItem(id), location, GeoEvent.Type.Moved));
            }

            @Override
            public void onGeoQueryReady() {}

            @Override
            public void onGeoQueryError(DatabaseError error) {}
        };

        query.addGeoQueryEventListener(listener);

    }

    protected void onNext (GeoEvent event) {
        eventPublishSubject.onNext(event);
        eventReplaySubject.onNext(event);
    }

    public Observable<GeoEvent> events () {
        return eventPublishSubject.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<GeoEvent> allEvents () {
        return eventReplaySubject.subscribeOn(Schedulers.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public void stopListeningForItems() {
        if (query != null) {
            query.removeAllListeners();
            query = null;
        }
    }

    public void removeItem(GeoItem item) {
        ref().child(item.getID()).removeValue();
    }

    public boolean updateLocation (double latitude, double longitude) {
        if (location != null) {
            // Work out the distance between the new location and the old one
            double distance = distanceBetween(new GeoLocation(latitude, longitude), location);
            if (distance < FirebaseNearbyUsersModule.config().minimumLocationChangeToUpdateServer) {
                return false;
            }
        }

        location = new GeoLocation(latitude, longitude);
        startListeningForItems(latitude, longitude, FirebaseNearbyUsersModule.config().maxDistance);

        return true;
    }

    public boolean addItemAtCurrentLocation(GeoItem item) {
        return addItemAtCurrentLocation(item, true);
    }

    public boolean addItemAtCurrentLocation(GeoItem item, boolean removeOnDisconnect) {
        if (location != null) {
            addItem(item, location.latitude, location.longitude, removeOnDisconnect);
            return true;
        }
        return false;
    }

    public void addItem(GeoItem item, double latitude, double longitude) {
        addItem(item, latitude, longitude, true);
    }

    public void addItem(GeoItem item, double latitude, double longitude, boolean removeOnDisconnect) {
        GeoLocation location = new GeoLocation(latitude, longitude);
        geoFireRef().setLocation(item.getID(), location, (key, error) -> {
            Logger.debug("Location updated");

            // Remove the location data when we disconnect
            if (removeOnDisconnect) {
                ref().child(item.getID()).onDisconnect().removeValue((databaseError, databaseReference) -> {
                    // Success
                    Logger.debug("Did add listener");
                });
            }
        });
    }

    public GeoFire geoFireRef () {
        return new GeoFire(ref());
    }

    public DatabaseReference ref () {
        return FirebasePaths.firebaseRef().child(FirebasePaths.LocationPath);
    }

    public static double distanceBetween (GeoLocation g0, GeoLocation g1) {
        // Work out the distance between the new location and the old one
        Location l0 = new Location("L0");
        l0.setLatitude(g0.latitude);
        l0.setLongitude(g0.longitude);

        Location l1 = new Location("L1");
        l1.setLatitude(g1.latitude);
        l1.setLongitude(g1.longitude);

        return l0.distanceTo(l1);

    }
}
