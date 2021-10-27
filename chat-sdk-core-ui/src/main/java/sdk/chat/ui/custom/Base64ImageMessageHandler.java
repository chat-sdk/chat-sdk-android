package sdk.chat.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.ImageMessageOnClickHandler;
import sdk.chat.ui.chat.model.Base64ImageMessageHolder;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.view_holders.IncomingBase64ImageMessageViewHolder;
import sdk.chat.ui.view_holders.OutcomingBase64ImageMessageViewHolder;

public class Base64ImageMessageHandler extends CustomMessageHandler {

    @Override
    public List<Byte> getTypes() {
        return types(MessageType.Base64Image);
    }

    @Override
    public boolean hasContentFor(MessageHolder holder) {
        return holder.getClass().equals(Base64ImageMessageHolder.class);
    }

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        holders.registerContentType(
                (byte) MessageType.Base64Image,
                IncomingBase64ImageMessageViewHolder.class,
                getAvatarClickPayload(context),
                R.layout.view_holder_incoming_image_message,
                OutcomingBase64ImageMessageViewHolder.class,
                getAvatarClickPayload(context),
                R.layout.view_holder_outcoming_image_message,
                ChatSDKUI.shared().getMessageCustomizer());
    }

    @Override
    public boolean onClick(Activity activity, View rootView, Message message) {
        if (!super.onClick(activity, rootView, message)) {
            if (message.typeIs(MessageType.Base64Image)) {

                String base64 = message.stringForKey(Keys.MessageImageData);
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                ImageMessageOnClickHandler.onClick(activity, rootView, bitmap);
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public MessageHolder onNewMessageHolder(Message message) {
        if (message.getMessageType().is(MessageType.Base64Image)) {
            return new Base64ImageMessageHolder(message);
        }
        return null;
    }
}
