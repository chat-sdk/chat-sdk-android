package co.chatsdk.firebase.nearby_users;

public class CustomProperties {

    /**
     * If this custom property is set it should contain an Double which is the number
     * of meters within which the exact distance isn't displayed. For example, if this
     * is set to 1000, if a user is within 1000m their exact distance won't be displayed
     * and <1000 will be displayed instead.
     */
    public static String MinimumResolution = "minimum-resolution";

}
