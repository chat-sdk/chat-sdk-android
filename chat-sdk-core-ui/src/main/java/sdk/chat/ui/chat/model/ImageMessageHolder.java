package sdk.chat.ui.chat.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

    @Nullable
    public String getImageUrl() {
        if (payload != null) {
            return payload.imageURL();
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

    @Nullable
    public Bitmap placeholder() {
        Bitmap bitmap = message.getPlaceholder();
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(ChatSDK.ctx().getResources(), R.drawable.icn_200_location_message_placeholder);
        }
//
//        if (message.typeIs(MessageType.Location)) {
//            return R.drawable.icn_200_location_message_placeholder;
//        }
//        return R.drawable.icn_200_image_message_placeholder;
        return bitmap;
    }

    public int defaultPlaceholder() {
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
