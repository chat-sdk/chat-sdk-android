package co.chatsdk.ui.chat;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.types.MessageSendStatus;

public class MessageListItem {

    protected static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ChatSDK.config().messageTimeFormat);

    public Message message;
    public float progress;

    public MessageListItem (Message message) {

        // If null that means no custom format was added to the adapter so we use the default.
        if (simpleDateFormat == null) {
            simpleDateFormat = getFormat(message);
        }

        this.message = message;

        message.valueForKey(Keys.MessageImageWidth);

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

    public LatLng getLatLng() {
        double longitude = objectToDouble(message.valueForKey(Keys.MessageLongitude));
        double latitude = objectToDouble(message.valueForKey(Keys.MessageLatitude));
        return new LatLng(latitude, longitude);
    }

    public Double objectToDouble (Object value) {
        if(value != null) {
            if(value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                }
                catch (NumberFormatException e) {
                    ChatSDK.logError(e);
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

    protected static SimpleDateFormat getFormat(Message message){

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

    public boolean equals (MessageListItem item) {
        return item.message.equals(message);
    }

    public long getTimeInMillis() {
        return message.getDate().toDate().getTime();
    }


}
