package sdk.chat.ui.chat.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;

public class Base64ImageMessageHolder extends MessageHolder implements MessageContentType {

    public Base64ImageMessageHolder(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return ChatSDK.imageMessage().toString(message);
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
        String base64 = message.stringForKey(Keys.MessageImageData);
        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

}
