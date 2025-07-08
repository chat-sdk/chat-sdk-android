package sdk.chat.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.ImageMessageOnClickHandler;
import sdk.chat.ui.chat.LocationMessageOnClickHandler;
import sdk.chat.ui.chat.model.ImageMessageHolder;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.view_holders.v2.outer.V2;

public class ImageMessageRegistration extends DefaultMessageRegistration {

    @Override
    public List<Byte> getTypes() {
        return types(MessageType.Image, MessageType.Location);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(ImageMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Image,
                V2.IncomingImageMessageViewHolder.class,
                R.layout.view_holder_incoming_image_message,
                V2.OutcomingImageMessageViewHolder.class,
                R.layout.view_holder_outcoming_image_message,
                ChatSDKUI.shared().getMessageRegistrationManager());
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        if (!super.onClick(activity, rootView, message)) {
            if (message.typeIs(MessageType.Image)) {
                ImageMessageOnClickHandler.onClick(activity, rootView, message.stringForKey(Keys.ImageUrl));
                return true;
            }
            else if (message.typeIs(MessageType.Location)) {
                double longitude = message.doubleForKey(Keys.MessageLongitude);
                double latitude = message.doubleForKey(Keys.MessageLatitude);

                Location location = new Location(ChatSDK.getString(sdk.chat.core.R.string.app_name));
                location.setLatitude(latitude);
                location.setLongitude(longitude);

                LocationMessageOnClickHandler.onClick(activity, location);
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.getMessageType().is(MessageType.Image, MessageType.Location)) {
            return new ImageMessageHolder(message);
        }
        return null;
    }

}
