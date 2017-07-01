package co.chatsdk.ui.chat;

import android.support.v7.app.AppCompatActivity;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.chatsdk.core.dao.BMessage;
import co.chatsdk.core.dao.BUser;
import co.chatsdk.core.types.Defines;
import co.chatsdk.core.utils.ImageUtils;
import co.chatsdk.ui.R;
import timber.log.Timber;

public class MessageListItem {

    // TODO: Move this to settings
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");

    public BMessage message;
    public float progress;
    private long timeInMillis;

    @Deprecated
    private int[] dimensions = null;

    public MessageListItem (BMessage message, int maxWidth) {

        // If null that means no custom format was added to the adapter so we use the default.
        if (simpleDateFormat == null)
            simpleDateFormat = getFormat(message);

        BUser user = message.getSender();

        this.message = message;
        timeInMillis = message.getDate().toDate().getTime();

        dimensions = getDimensions(maxWidth);

    }

    public String getEntityID () {
        return message.getEntityID();
    }

    public String getTime () {
        return String.valueOf(simpleDateFormat.format(message.getDate().toDate()));
    }

    public String getText () {
        return message.getText();
    }

    public boolean isMine () {
        return message.getSender().isMe();
    }

    public int messageType () {
        return message.getType();
    }

    public long getId () {
        return message.getId();
    }

    public boolean delivered () {
        return message.wasDelivered() == BMessage.Delivered.Yes;
    }

    public Integer status () {
        return message.getStatusOrNull();
    }

    public int width () {
        return dimensions[0];
    }

    public String getProfilePicUrl () {
        return message.getSender().getMetaPictureUrl();
    }

    public int height () {
        return dimensions[1];
    }

    private static SimpleDateFormat getFormat(BMessage message){

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

    @Deprecated
    private int[] getDimensions(int maxWidth){

        if (StringUtils.isNotEmpty(getText())) {

            try {
                String[] data = getText().split(Defines.DIVIDER);
                dimensions = ImageUtils.getDimensionsFromString(data[data.length - 1]);
                dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);

                if (dimensions.length != 2)
                    dimensions = null;

            }catch (Exception e){  dimensions = null;}

        }
        else if (StringUtils.isNotEmpty(message.getImageDimensions())) {

            dimensions = ImageUtils.getDimensionsFromString(message.getImageDimensions());

        }

        return dimensions;
    }

    public boolean isValid () {
        if(messageType() == BMessage.Type.IMAGE && dimensions == null) {
            return false;
        }
        return true;
    }

    public boolean equals (MessageListItem item) {
        return item.message.equals(message);
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }


}
