package sdk.chat.core.manager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import sdk.chat.core.R;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Base64ImageUtils;
import sdk.chat.core.utils.ImageMessageUtil;
import sdk.chat.core.utils.Size;

public class ImageMessagePayload extends AbstractMessagePayload {

    protected Size size;

    public ImageMessagePayload(Message message) {
        super(message);
        this.size = getSize();
    }

    @Override
    public String getText() {
        return message.stringForKey(Keys.MessageImageURL);
    }

    @Override
    public String imageURL() {
        return message.getImageURL();
    }

//    @Override
//    public List<String> remoteURLs() {
//        List<String> urls = new ArrayList<>();
//        String url = message.stringForKey(Keys.MessageImageURL);
//        if (url != null) {
//            urls.add(url);
//        }
//        return urls;
//    }

    @Override
    public String lastMessageText() {
        return ChatSDK.getString(R.string.image_message);
    }

    @Override
    public Drawable getPlaceholder() {
        Bitmap bitmap = null;
        if (message.getPlaceholderPath() != null) {
            bitmap = BitmapFactory.decodeFile(message.getPlaceholderPath());
        }
        if (bitmap == null) {
            String base64 = message.stringForKey(Keys.MessageImagePreview);
            if (base64 != null) {
                bitmap = Base64ImageUtils.fromBase64(base64);
            }
        }
        if (bitmap != null) {
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, size.widthInt(), size.heightInt());
            return new BitmapDrawable(ChatSDK.ctx().getResources(), bitmap);
        }
        return null;
    }

    protected Integer width() {
        Object width = message.valueForKey(Keys.MessageImageWidth);
        if (width instanceof Integer) {
            return (Integer) width;
        }
        return null;
    }

    protected Integer height() {
        Object height = message.valueForKey(Keys.MessageImageHeight);
        if (height instanceof Integer) {
            return (Integer) height;
        }
        return null;
    }

    public Size getSize() {
        if (size == null) {
            Integer width = width();
            Integer height = height();
            if (width != null && height != null) {
                size = ImageMessageUtil.getImageMessageSize(width, height);
            }
        }
        return size;
    }


}
