package sdk.chat.location;

import com.firebase.geofire.GeoLocation;

public class GeoItem {

    public static String USER = "user";
    public static String THREAD = "thread";

    protected String entityID;

    // Set this as the default for backwards compatibility
    protected String type = USER;

    protected GeoLocation location = null;

    public GeoItem (String entityID, String type) {
        this.entityID = entityID;
        this.type = type;
    }

    public GeoItem (String id, GeoLocation location) {
        this(id);
        this.location = location;
    }

    public GeoItem (String id) {
        String [] split = id.split("_");
        if (split.length >= 2) {
            this.type = split[0];
            this.entityID = id.replace(this.type + "_", "");
        } else {
            entityID = id;
        }
    }

    public String getID () {
        return type + "_" + entityID;
    }

    public boolean isType (String type) {
        return this.type.equals(type);
    }

    public String getEntityID () {
        return entityID;
    }

    public String getType () {
        return type;
    }

    @Override
    public boolean equals(Object item) {
        return item instanceof GeoItem && entityID.equals(((GeoItem)item).getEntityID());
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }
}
