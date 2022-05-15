package sdk.chat.ui.chat.model;

import android.content.Context;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import io.reactivex.Single;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;

public class ImageMessageHolder extends MessageHolder implements MessageContentType, MessageContentType.Image {

    public ImageMessageHolder(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        if (message.getMessageType().is(MessageType.Image)) {
            return ChatSDK.imageMessage().toString(message);
        }
        if (message.getMessageType().is(MessageType.Location)) {
            return ChatSDK.locationMessage().toString(message);
        }
        return super.getText();
    }

    @Nullable
    public String getImageUrl() {
        if (message.getMessageType().is(MessageType.Image, MessageType.Location, MessageType.Video)) {
            return message.getImageURL();
        }
        return null;
    }

    public Integer width() {
        Object width = message.valueForKey(Keys.MessageImageWidth);
        if (width instanceof Integer) {
            return (Integer) width;
        }
        return null;
    }

    public Integer height() {
        Object height = message.valueForKey(Keys.MessageImageHeight);
        if (height instanceof Integer) {
            return (Integer) height;
        }
        return null;
    }

    public int placeholder() {
        if (message.typeIs(MessageType.Location)) {
            return R.drawable.icn_200_location_message_placeholder;
        }
        return R.drawable.icn_200_image_message_placeholder;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public Single<String> save(Context context) {
        return ChatSDKUI.provider().saveProvider().saveImage(context, getImageUrl());
    }
}
