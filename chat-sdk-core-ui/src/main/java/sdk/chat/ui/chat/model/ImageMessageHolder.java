package sdk.chat.ui.chat.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import io.reactivex.Single;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.ImageMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Size;
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
    public Drawable placeholder() {
        Drawable drawable = null;
        if (payload != null) {
            drawable = payload.getPlaceholder();
        }
        if (drawable == null) {
            drawable = AppCompatResources.getDrawable(ChatSDK.ctx(), defaultPlaceholder());
        }
        return drawable;
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

    public ImageMessagePayload getPayload() {
        if (payload instanceof ImageMessagePayload) {
            return (ImageMessagePayload) payload;
        }
        return null;
    }

    public Size getSize() {
        ImageMessagePayload payload = getPayload();
        if (payload != null) {
            return payload.getSize();
        }
        return new Size(0, 0);
    }

}
