package sdk.chat.location;

import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.firebase.FirebasePaths;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import sdk.guru.common.RX;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by ben on 4/3/18.
 */

public class GeoFireManager {

    protected GeoQuery query;

    protected PublishSubject<GeoEvent> eventPublishSubject = PublishSubject.create();
    protected ReplaySubject<GeoEvent> eventReplaySubject = ReplaySubject.create();

    protected static GeoFireManager shared = new GeoFireManager();

    protected Map<String, GeoItem> itemMap = new HashMap<>();

    public static GeoFireManager shared () {
        return shared;
    }


    public GeoFireManager () {
    }

    public void startListeningForItems(double latitude, double longitude, float radius) {

        stopListeningForItems();

        GeoFire geoFire = geoFireRef();
        GeoLocation center = new GeoLocation(latitude, longitude);
        query = geoFire.queryAtLocation(center, radius);

        GeoQueryEventListener listener = new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String id, GeoLocation location) {
                updateItem(id, location, GeoEvent.Type.Entered);
            }

            @Override
            public void onKeyExited(String id) {
                updateItem(id, null, GeoEvent.Type.Exited);
            }

            @Override
            public void onKeyMoved(String id, GeoLocation location) {
                updateItem(id, location, GeoEvent.Type.Moved);
            }

            @Override
            public void onGeoQueryReady() {}

            @Override
            public void onGeoQueryError(DatabaseError error) {}
        };

        query.addGeoQueryEventListener(listener);

    }

    protected void updateItem(String id, GeoLocation location, GeoEvent.Type type) {
        GeoItem item = itemMap.get(id);
        if (type == GeoEvent.Type.Entered && item == null) {
            item = new GeoItem(id, location);
            itemMap.put(id, item);
        }
        if (type == GeoEvent.Type.Moved && item != null) {
            item.setLocation(location);
        }
        if (type == GeoEvent.Type.Exited) {
            itemMap.remove(id);
        }
        onNext(new GeoEvent(item, type));
    }

    protected void onNext (GeoEvent event) {
        eventPublishSubject.onNext(event);
        eventReplaySubject.onNext(event);
    }

    public Observable<GeoEvent> events () {
        return eventPublishSubject.subscribeOn(RX.single()).observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<GeoEvent> allEvents () {
        return eventReplaySubject.subscribeOn(RX.single()).observeOn(AndroidSchedulers.mainThread());
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

    Map<String, GeoItem> getItemMap() {
        return itemMap;
    }
}
