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
    public String entityId, profilePicUrl, time, text, resourcePath;
    private long timeInMillis;
    private int[] dimensions = null;
    private String dimensionsString;

    public MessageListItem (BMessage message, int maxWidth) {

        // If null that means no custom format was added to the adapter so we use the default.
        if (simpleDateFormat == null)
            simpleDateFormat = getFormat(message);

        BUser user = message.getSender();

        this.message = message;
        entityId = message.getEntityID();
        profilePicUrl = user.getThumbnailPictureURL();
        time = String.valueOf(simpleDateFormat.format(message.getDate().toDate()));
        text = message.getTextString();
        resourcePath = message.getResourcesPath();
        this.dimensionsString = message.getImageDimensions();
        timeInMillis = message.getDate().toDate().getTime();

        dimensions = getDimensions(maxWidth);

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

    public int height () {
        return dimensions[1];
    }

    public static List<MessageListItem> makeList(AppCompatActivity activity, List<BMessage> messages){

        List<MessageListItem> list = new ArrayList<>();

        int maxWidth =  (activity.getResources().getDimensionPixelSize(R.dimen.chat_sdk_max_image_message_width));

        MessageListItem item;
        for (BMessage message : messages)
        {
            item = new MessageListItem(message, maxWidth);

            if (message.getType() != BMessage.Type.TEXT && item.dimensions == null)
            {
                Timber.d("Cant find dimensions, path: %s, dimensionsString: %s", item.resourcePath, item.dimensionsString);
                continue;
            }

            // Skip messages with no date
            if (message.getDate() == null)
                continue;

            list.add(item);
        }

        // We need to reverse the list so the newest bundle would be on the top again.
        Collections.reverse(list);

        return list;
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

    private static String getUrl(String text, int type){

        if (StringUtils.isBlank(text))
            return "";

        String url = "";
        String [] urls = text.split(Defines.DIVIDER);
        if (type == BMessage.Type.IMAGE)
        {
            if (urls.length > 1)
            {
                url = urls[1];
            }
            else url = urls[0];
        }
        else if (type == BMessage.Type.LOCATION)
        {
            if (urls.length == 1)
                urls = text.split("&");

            try {
                if (urls.length > 3)
                    url = urls[3];
                else url = urls[2];
            } catch (Exception e) {
//                e.printStackTrace();
            }
        }

        return url;
    }

    private int[] getDimensions(int maxWidth){

        if (StringUtils.isNotEmpty(text)) {

            try {
                String[] data = text.split(Defines.DIVIDER);
                dimensions = ImageUtils.getDimensionsFromString(data[data.length - 1]);
                dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);

                if (dimensions.length != 2)
                    dimensions = null;

            }catch (Exception e){  dimensions = null;}

        }
        else if (StringUtils.isNotEmpty(dimensionsString)) {

            dimensions = ImageUtils.getDimensionsFromString(dimensionsString);
            dimensions = ImageUtils.calcNewImageSize(dimensions, maxWidth);

        }

        return dimensions;
    }

    public boolean equals (MessageListItem item) {
        return item.message.equals(message);
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }


}
