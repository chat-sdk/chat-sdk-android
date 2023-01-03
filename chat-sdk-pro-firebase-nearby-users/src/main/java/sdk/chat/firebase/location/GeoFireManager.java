package sdk.chat.firebase.location;

import android.location.Location;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.jakewharton.rxrelay2.PublishRelay;
import com.jakewharton.rxrelay2.ReplayRelay;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import sdk.chat.core.dao.User;
import sdk.chat.core.hook.Hook;
import sdk.chat.core.hook.HookEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.firebase.adapter.FirebasePaths;
import sdk.guru.common.RX;

/**
 * Created by ben on 4/3/18.
 */

public class GeoFireManager {

    protected GeoQuery query;

    protected PublishRelay<GeoEvent> eventPublishRelay = PublishRelay.create();
    protected ReplayRelay<GeoEvent> eventReplayRelay = ReplayRelay.create();
    protected PublishRelay<List<LocationUser>> locationUsersPublishRelay = PublishRelay.create();

    protected Map<String, GeoItem> itemMap = new HashMap<>();

    protected List<LocationUser> locationUsers = new ArrayList<>();

    public GeoFireManager() {
        allEvents().observeOn(RX.db()).doOnNext(geoEvent -> {
            if (geoEvent.item.isType(GeoItem.USER)) {
                String entityID = geoEvent.item.entityID;
                User user = ChatSDK.core().getUserNowForEntityID(entityID);
                if (!user.isMe()) {
                    LocationUser lu = getLocationUser(entityID);

                    boolean updated = true;

                    if (geoEvent.type.equals(GeoEvent.Type.Entered) && lu == null) {
                        locationUsers.add(new LocationUser(user, geoEvent.getLocation()));
                    }
                    else if (geoEvent.type.equals(GeoEvent.Type.Exited) && lu != null) {
                        locationUsers.remove(lu);
                    }
                    else if (geoEvent.type.equals(GeoEvent.Type.Moved) && lu != null) {
                        lu.location = geoEvent.getLocation();
                    } else {
                        updated = false;
                    }

                    if (updated) {
                        locationUsersPublishRelay.accept(locationUsers);
                    }
                }
            }
        }).subscribe();

        ChatSDK.hook().addHook(Hook.sync(data -> {
            locationUsers.clear();
        }), HookEvent.DidLogout);

    }

    public List<LocationUser> getLocationUsers() {
        return locationUsers;
    }

    public LocationUser getLocationUser(String entityID) {
        for (LocationUser lu : locationUsers) {
            if (lu.user.getEntityID().equals(entityID)) {
                return lu;
            }
        }
        return null;
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
        eventPublishRelay.accept(event);
        eventReplayRelay.accept(event);
    }

    public Observable<GeoEvent> events() {
        return eventPublishRelay.observeOn(RX.main());
    }

    public Observable<GeoEvent> allEvents() {
        return eventReplayRelay.observeOn(RX.main());
    }

    public Observable<List<LocationUser>> locationUsersEvents() {
        return locationUsersPublishRelay.observeOn(RX.main());
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
        RX.io().scheduleDirect(() -> {
            GeoLocation location = new GeoLocation(latitude, longitude);
            geoFireRef().setLocation(item.getID(), location, (key, error) -> {
                Logger.info("NEARBY USERS: Location updated");

                // Remove the location data when we disconnect
                if (removeOnDisconnect) {
                    ref().child(item.getID()).onDisconnect().removeValue((databaseError, databaseReference) -> {
                        // Success
                        Logger.debug("NEARBY USERS: Did add listener");
                    });
                }
            });
        });
    }

    public GeoFire geoFireRef () {
        return new GeoFire(ref());
    }

    public DatabaseReference ref () {
        return FirebasePaths.firebaseRef().child(FirebasePaths.LocationPath);
    }

    public static double distanceBetween(GeoLocation g0, GeoLocation g1) {
        // Work out the distance between the new location and the old one
        Location l0 = new Location("L0");
        l0.setLatitude(g0.latitude);
        l0.setLongitude(g0.longitude);

        Location l1 = new Location("L1");
        l1.setLatitude(g1.latitude);
        l1.setLongitude(g1.longitude);

        return l0.distanceTo(l1);

    }

    public void stop() {
        locationUsers.clear();
        itemMap.clear();
        locationUsersPublishRelay.accept(locationUsers);
    }

    Map<String, GeoItem> getItemMap() {
        return itemMap;
    }
}
