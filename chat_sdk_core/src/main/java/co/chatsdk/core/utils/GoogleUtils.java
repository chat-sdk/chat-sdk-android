package co.chatsdk.core.utils;

/**
 * Created by benjaminsmiley-andrews on 09/06/2017.
 */

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import wanderingdevelopment.tk.chatsdkcore.R;

public class GoogleUtils {

    public static String getMapImageURL (LatLng location, int width, int height) {

        String googleMapsAPIKey = AppContext.googleMapsAPIKey;

        String api = "https://maps.googleapis.com/maps/api/staticmap";
        String markers = "markers="+location.latitude+","+location.longitude;
        String size = "zoom=20&size="+width+"x"+ height;
        String key = "key=" + googleMapsAPIKey;

        return api + "?" + markers + "&" + size + "&" + key;
    }

}
