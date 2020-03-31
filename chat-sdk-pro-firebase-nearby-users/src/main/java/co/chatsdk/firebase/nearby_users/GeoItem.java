package co.chatsdk.firebase.nearby_users;

public class GeoItem {

    public static String USER = "user";
    public static String THREAD = "thread";

    protected String entityID;

    // Set this as the default for backwards compatibility
    protected String type = USER;

    public GeoItem (String entityID, String type) {
        this.entityID = entityID;
        this.type = type;
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


}
