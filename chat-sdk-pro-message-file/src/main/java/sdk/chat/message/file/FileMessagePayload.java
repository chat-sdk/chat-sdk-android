package sdk.chat.message.file;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.core.content.ContextCompat;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.DownloadablePayload;
import sdk.chat.core.manager.TextMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.Base64ImageUtils;
import sdk.chat.core.utils.StringChecker;

public class FileMessagePayload extends TextMessagePayload implements DownloadablePayload {

    public FileMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        String name = message.getText();
        if (StringChecker.isNullOrEmpty(name)) {
            name = ChatSDK.getString(R.string.file_message);
        }
        return name;
    }

    @Override
    public String lastMessageText() {
        return getText();
    }

    public String fileURL() {
        return message.stringForKey(Keys.MessageFileURL);
    }

    @Override
    public String imageURL() {
        if (message.getMessageType().is(MessageType.File) || message.getReplyType().is(MessageType.File)) {
            String imageURL = message.getImageURL();
            if (imageURL != null && !imageURL.isEmpty()) {
                return imageURL;
            } else {

                Context context = ChatSDK.ctx();
                Resources resources = context.getResources();

                final String mimeType = message.stringForKey(Keys.MessageMimeType);
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

                int resID = resources.getIdentifier("file_type_" + extension, "drawable", context.getPackageName());
                resID = resID > 0 ? resID : R.drawable.file;

                Uri uri = new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(resources.getResourcePackageName(resID))
                        .appendPath(resources.getResourceTypeName(resID))
                        .appendPath(resources.getResourceEntryName(resID))
                        .build();

                return uri.toString();
            }
        }
        return null;
    }

    @Override
    public Drawable getPlaceholder() {
        Context context = ChatSDK.ctx();

        final String mimeType = message.stringForKey(Keys.MessageMimeType);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

        int resID = context.getResources().getIdentifier("file_type_" + extension, "drawable", context.getPackageName());
        resID = resID > 0 ? resID : R.drawable.file;

        return ContextCompat.getDrawable(context, resID);
    }

    @Override
    public TransferStatus downloadStatus() {
        if (message.getFilePath() != null) {
            return TransferStatus.Complete;
        }
        return ChatSDK.downloadManager().getDownloadStatus(message);
    }

    @Override
    public boolean canDownload() {
        return downloadStatus() == TransferStatus.None && fileURL() != null;
    }

    @Override
    public Completable startDownload() {
        return Completable.create(emitter -> {
            if (canDownload()) {
                ChatSDK.downloadManager().download(message, Keys.MessageFileURL, fileURL(), "File_" + message.getEntityID());
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
}
