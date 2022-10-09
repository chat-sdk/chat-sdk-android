package sdk.chat.message.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.image.ImageUtils;
import sdk.chat.core.manager.DownloadablePayload;
import sdk.chat.core.manager.ImageMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;
import sdk.chat.core.utils.Base64ImageUtils;

public class VideoMessagePayload extends ImageMessagePayload implements DownloadablePayload {

    public VideoMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return message.stringForKey(Keys.MessageVideoURL);
    }

    public String videoURL() {
        return message.stringForKey(Keys.MessageVideoURL);
    }

    @Override
    public boolean canDownload() {
        return downloadStatus() == TransferStatus.None && videoURL() != null;
    }

    @Override
    public Completable startDownload() {
        return Completable.create(emitter -> {
            if (canDownload()) {
                ChatSDK.downloadManager().download(message, Keys.MessageVideoURL, videoURL(), "Video_" + message.getEntityID());
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(ChatSDK.getString(R.string.download_failed)));
            }
        });
    }

    @Override
    public Integer size() {
        Object size = message.valueForKey(Keys.MessageSize);
        if (size instanceof Integer) {
            return (Integer) size;
        }
        return null;
    }

    @Override
    public String lastMessageText() {
        return ChatSDK.getString(R.string.video_message);
    }

    @Override
    public TransferStatus downloadStatus() {
        if (message.getFilePath() != null) {
            return TransferStatus.Complete;
        }
        return ChatSDK.downloadManager().getDownloadStatus(message);
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
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, size.widthInt(), size.heightInt(), ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
        return new BitmapDrawable(ChatSDK.ctx().getResources(), bitmap);
    }
}
