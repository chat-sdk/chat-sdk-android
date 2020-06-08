package sdk.chat.firebase.location;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

import sdk.guru.common.DisposableMap;

public class GeoItemManager {
    protected static final GeoItemManager instance = new GeoItemManager();

    protected final DisposableMap dm = new DisposableMap();

    protected final List<GeoItem> trackedItems = new ArrayList<>();

    public static GeoItemManager shared() {
        return instance;
    }

    public GeoItemManager() {
        dm.add(LocationHandler.shared().getLocationUpdateWhenMinDistance().subscribe(location -> {
            for (GeoItem item: trackedItems) {
                pushToGeoFire(item, location, true);
            }
        }));
    }

    public void addOneTimeItem(GeoItem item) {
        dm.add(LocationHandler.shared().once().subscribe(location -> {
            pushToGeoFire(item, location, false);
        }));
    }

    public void addTrackedItem(GeoItem item, boolean force) {
        if (!trackedItems.contains(item) || force) {
            dm.add(LocationHandler.shared().once().subscribe(location -> {
                pushToGeoFire(item, location, true);
            }));
        }
        if (!trackedItems.contains(item)) {
            trackedItems.add(item);
        }
    }

    protected void pushToGeoFire(GeoItem item, Location location, boolean removeOnDisconnect) {
        GeoFireManager.shared().addItem(item, location.getLatitude(), location.getLongitude(), removeOnDisconnect);
    }

    protected void removeFromGeoFire(GeoItem item) {
        trackedItems.remove(item);
        GeoFireManager.shared().removeItem(item);
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
