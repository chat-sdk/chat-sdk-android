package co.chatsdk.ui.utils;

import android.content.Context;
import android.graphics.Bitmap;

import co.chatsdk.core.defines.Availability;
import co.chatsdk.ui.R;

/**
 * Created by ben on 8/23/17.
 */

public class AvailabilityHelper {

    public static String stringForAvailability (Context context, String availability) {

        String availabilityString = null;

        if(availability.equals(Availability.Unavailable)) {
            availabilityString = context.getString(R.string.availability_unavailable);
        }
        if(availability.equals(Availability.Available)) {
            availabilityString = context.getString(R.string.availability_unavailable);
        }
        if(availability.equals(Availability.Busy)) {
            availabilityString = context.getString(R.string.availability_busy);
        }
        if(availability.equals(Availability.Away)) {
            availabilityString = context.getString(R.string.availability_away);
        }
        if(availability.equals(Availability.XA)) {
            availabilityString = context.getString(R.string.availability_extended_away);
        }

        return availabilityString;

    }

    public static int imageResourceIdForAvailability (String availability) {
        int imageId = 0;

        if(availability == null || availability.equals(Availability.Unavailable)) {
            imageId = R.drawable.icn_20_offline;
        }
        else if(availability.equals(Availability.Available)) {
            imageId = R.drawable.icn_20_online;
        }
        else if(availability.equals(Availability.Busy)) {
            imageId = R.drawable.icn_20_busy;
        }
        else if(availability.equals(Availability.Away)) {
            imageId = R.drawable.icn_20_away;
        }
        else if(availability.equals(Availability.XA)) {
            imageId = R.drawable.icn_20_xa;
        }

        return imageId;
    }

}
