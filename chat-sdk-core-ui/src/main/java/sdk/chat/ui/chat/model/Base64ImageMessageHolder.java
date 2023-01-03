package sdk.chat.ui.chat.model;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.utils.Base64ImageUtils;
import sdk.chat.ui.R;

public class Base64ImageMessageHolder extends MessageHolder implements MessageContentType {

    public Base64ImageMessageHolder(Message message) {
        super(message);
    }

    @Nullable
    public String getImageUrl() {
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
        return R.drawable.icn_200_image_message_placeholder;
    }

    public Bitmap image() {
        return Base64ImageUtils.fromBase64(message.stringForKey(Keys.MessageImageData));
    }

}
