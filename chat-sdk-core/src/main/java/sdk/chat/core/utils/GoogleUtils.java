package sdk.chat.core.utils;

/**
 * Created by benjaminsmiley-andrews on 09/06/2017.
 */

import android.location.Location;

import sdk.chat.core.session.ChatSDK;

public class GoogleUtils {

    public static String getMapImageURL(double latitude, double longitude, int width, int height) {

        String googleMapsAPIKey = ChatSDK.config().googleMapsApiKey;

        String api = "https://maps.googleapis.com/maps/api/staticmap";
        String markers = "markers="+latitude+","+longitude;
        String size = "zoom=18&size="+width+"x"+ height;
        String key = "key=" + googleMapsAPIKey;

        return api + "?" + markers + "&" + size + "&" + key;
    }

    public static String getMapImageURL(Location location, int width, int height) {
        return getMapImageURL(location.getLatitude(), location.getLongitude(), width, height);
    }

    public static String getMapWebURL(Location location) {
        return "http://maps.google.com/maps?z=12&t=m&q=loc:" + location.getLatitude() + "+" + location.getLongitude();
    }

}
