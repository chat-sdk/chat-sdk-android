package sdk.chat.message.file;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.AbstractMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.StringChecker;

public class FileMessagePayload extends AbstractMessagePayload {

    public FileMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
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

                int resID = context.getResources().getIdentifier("file_type_" + extension, "drawable", context.getPackageName());
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

//    @Override
//    public List<String> remoteURLs() {
//        List<String> urls = new ArrayList<>();
//        String imageURL = message.stringForKey(Keys.MessageImageURL);
//        if (imageURL != null) {
//            urls.add(imageURL);
//        }
//        String fileURL = message.stringForKey(Keys.MessageFileURL);
//        if (fileURL != null) {
//            urls.add(fileURL);
//        }
//        return urls;
//    }

//    @Override
//    public Completable downloadMessageContent() {
//        return Completable.create(emitter -> {
//            // TODO:
//        });
//    }

    @Override
    public String toString() {
        String text = super.toString();
        if (StringChecker.isNullOrEmpty(text)) {
            text = ChatSDK.getString(R.string.file_message);
        }
        return text;
    }
}
