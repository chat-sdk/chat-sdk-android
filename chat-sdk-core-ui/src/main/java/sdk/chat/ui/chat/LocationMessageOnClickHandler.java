package sdk.chat.ui.chat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;

import java.util.Locale;

import sdk.chat.ui.R;
import sdk.chat.ui.utils.ToastHelper;

/**
 * Created by benjaminsmiley-andrews on 20/06/2017.
 */

public class LocationMessageOnClickHandler {

    public static void onClick (Activity activity, Location latLng) {
        try {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f (%s)", latLng.getLatitude(), latLng.getLongitude(), latLng.getLatitude(), latLng.getLongitude(), "Mark");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastHelper.show(activity, R.string.message_adapter_no_google_maps);
        }
    }

}
