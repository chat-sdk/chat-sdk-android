package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.ToastHelper;

/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class LocationMessageOnClickHandler {

    public static void onClick (Activity activity, LatLng latLng) {
        try {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f (%s)", latLng.latitude, latLng.longitude, latLng.latitude, latLng.longitude, "Mark");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastHelper.show(activity, R.string.message_adapter_no_google_maps);
        }
    }

}
