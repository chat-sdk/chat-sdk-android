package co.chatsdk.ui.chat;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageSendStatus;
import timber.log.Timber;

public class MessageListItem {

    // TODO: Move this to settings
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    public Message message;
    public float progress;
    private long timeInMillis;

//    @Deprecated
//    private int[] dimensions = null;

    public MessageListItem (Message message, int maxWidth) {

        // If null that means no custom format was added to the adapter so we use the default.
        if (simpleDateFormat == null)
            simpleDateFormat = getFormat(message);

        this.message = message;

        if (message.getDate() != null) {
            timeInMillis = message.getDate().toDate().getTime();
        }
        else {
            Timber.v("");
        }

        message.valueForKey(Keys.MessageImageWidth);

//        // TODO: This is only here for backwards compatibility
//        dimensions = getDimensions(maxWidth);

    }

    public String getEntityID () {
        return message.getEntityID();
    }

    public String getTime () {
        return String.valueOf(simpleDateFormat.format(message.getDate().toDate()));
    }

    public long getId () {
        return message.getId();
    }

    public boolean statusIs (MessageSendStatus status) {
        return getMessage().getMessageStatus() == status;
    }

    public String getImageURL () {
        return (String) message.valueForKey(Keys.MessageImageURL);
    }

    public MessageSendStatus status () {
        return message.getMessageStatus();
    }
//
//    public int width () {
//        Object width = message.valueForKey(Keys.MessageImageWidth);
//        int w = objectToInteger(width);
//        if(w > 0) {
//            return w;
//        }
//        return dimensions[0];
//    }

    public LatLng getLatLng() {
        double longitude = objectToDouble(message.valueForKey(Keys.MessageLongitude));
        double latitude = objectToDouble(message.valueForKey(Keys.MessageLatitude));
        return new LatLng(latitude, longitude);
    }

//    public int height () {
//        Object height = message.valueForKey(Keys.MessageImageHeight);
//        int h = objectToInteger(height);
//        if(h > 0) {
//            return h;
//        }
//        return dimensions[1];
//    }

    public Double objectToDouble (Object value) {
        if(value != null) {
            if(value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                }
                catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            if(value instanceof Double) {
                return (Double) value;
            }
        }
        return 0.0;
    }

    public Message getMessage () {
        return message;
    }

    private static SimpleDateFormat getFormat(Message message){

        Date curTime = new Date();
        long interval = (curTime.getTime() - message.getDate().toDate().getTime()) / 1000L;

        // More then a day ago
        if (interval > 3600 * 24)
        {
            // More then a year
            if (interval > 3600 * 24 * 365)
            {
                simpleDateFormat.applyPattern(Defines.MessageDateFormat.YearOldMessageFormat);
                return simpleDateFormat;
            }
            else {
                simpleDateFormat.applyPattern(Defines.MessageDateFormat.DayOldFormat);
                return simpleDateFormat;
            }
        }
        else
        {
            simpleDateFormat.applyPattern(Defines.MessageDateFormat.LessThenDayFormat);
            return simpleDateFormat;
        }
    }

//    @Deprecated
//    private int[] getDimensions(int maxWidth){
//
//        if (StringUtils.isNotEmpty(message.getTextString())) {
//
//            // Text comes in the form: url1, url2, W[width]&H[height]
//            try {
//                String[] data = message.getTextString().split(Defines.DIVIDER);
//                dimensions = ImageUtils.getDimensionsFromString(data[data.length - 1]);
//                dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);
//
//                if (dimensions.length != 2)
//                    dimensions = null;
//
//            }
//            catch (Exception e){  dimensions = null;}
//        }
//
//        return dimensions;
//    }

//    public boolean isValid () {
//        if(message.getMessageType() == MessageType.Image && dimensions == null) {
//            return false;
//        }
//        return true;
//    }

    public boolean equals (MessageListItem item) {
        return item.message.equals(message);
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }


}
