package sdk.chat.location;

import com.firebase.geofire.GeoLocation;

public class GeoEvent {

    public enum Type {
        Entered,
        Moved,
        Exited,
    }

    public GeoItem item;
    public Type type;

    public GeoEvent (GeoItem item, Type type) {
        this.item = item;
        this.type = type;
    }

    public GeoLocation getLocation() {
        if (item != null) {
            return item.getLocation();
        }
        return null;
    }
}
