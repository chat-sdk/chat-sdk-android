package sdk.chat.message.sticker;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.AbstractMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.message.sticker.provider.PListStickerPackProvider;

public class StickerMessagePayload extends AbstractMessagePayload {

    public StickerMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        String name = message.stringForKey(Keys.MessageStickerName);
        String [] parts = name.split(".");
        if (parts.length == 2) {
            return parts[0];
        }
        return name;
    }

    @Override
    public String imageURL() {
        if (message.getMessageType().is(MessageType.Sticker)  || message.getReplyType().is(MessageType.Sticker)) {
            Context context = ChatSDK.ctx();
            Resources resources = context.getResources();
            String stickerName = (String) message.valueForKey(Keys.MessageStickerName);

            Logger.debug("Sticker:" + System.identityHashCode(message) + ", " + message.getMetaValuesAsMap() + ", " + stickerName);

            // Do this because otherwise we get a crash because the message
            // holder tries to update before it's ready
            if (!StringChecker.isNullOrEmpty(stickerName)) {
                int resID = PListStickerPackProvider.resourceId(context, stickerName);

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
    public List<String> remoteURLs() {
        List<String> urls = new ArrayList<>();
        // TODO: We need to support remote URLs and local URLs
        String url = message.stringForKey(Keys.MessageImageURL);
        if (url != null) {
            urls.add(url);
        }
        return urls;
    }

    @Override
    public Completable downloadMessageContent() {
        return Completable.create(emitter -> {
            // TODO:
        });
    }

    @Override
    public String previewText() {
        String text = super.toString();
        if (StringChecker.isNullOrEmpty(text)) {
            text = ChatSDK.getString(R.string.sticker_message);
        }
        return text;
    }}
