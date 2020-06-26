package sdk.chat.firebase.location;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import sdk.guru.common.DisposableMap;

public class GeoItemManager {

    protected final DisposableMap dm = new DisposableMap();

    protected final List<GeoItem> trackedItems = new ArrayList<>();

    public GeoItemManager() {
        dm.add(FirebaseNearbyUsersModule.shared().getLocationHandler().getLocationUpdateWhenMinDistance().subscribe(location -> {
            for (GeoItem item: trackedItems) {
                pushToGeoFire(item, location, true);
            }
        }));
    }

    public void addOneTimeItem(GeoItem item) {
        dm.add(FirebaseNearbyUsersModule.shared().getLocationHandler().once().subscribe(location -> {
            pushToGeoFire(item, location, false);
        }));
    }

    public void addTrackedItem(GeoItem item, boolean force) {
        if (!trackedItems.contains(item) || force) {
            dm.add(FirebaseNearbyUsersModule.shared().getLocationHandler().once().subscribe(location -> {
                pushToGeoFire(item, location, true);
            }));
        }
        if (!trackedItems.contains(item)) {
            trackedItems.add(item);
        }
    }

    protected void pushToGeoFire(GeoItem item, Location location, boolean removeOnDisconnect) {
        FirebaseNearbyUsersModule.shared().getGeoFireManager().addItem(item, location.getLatitude(), location.getLongitude(), removeOnDisconnect);
    }

    protected void removeFromGeoFire(GeoItem item) {
        trackedItems.remove(item);
        FirebaseNearbyUsersModule.shared().getGeoFireManager().removeItem(item);
    }

    protected void remove(GeoItem item) {
        trackedItems.remove(item);
    }

    protected void clear() {
        for (GeoItem item: trackedItems) {
            removeFromGeoFire(item);
        }
        trackedItems.clear();
    }
}
