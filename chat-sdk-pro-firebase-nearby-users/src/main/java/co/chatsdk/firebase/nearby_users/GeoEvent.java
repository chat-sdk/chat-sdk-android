package co.chatsdk.firebase.nearby_users;

import com.firebase.geofire.GeoLocation;

public class GeoEvent {

    public enum Type {
        Entered,
        Moved,
        Exited,
    }

    public GeoItem item;
    public GeoLocation location;
    public Type type;

    public GeoEvent (GeoItem item, GeoLocation location, Type type) {
        this.item = item;
        this.location = location;
        this.type = type;
    }
}
