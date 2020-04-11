package co.chatsdk.message.sticker.integration;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;

import androidx.annotation.Nullable;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.message.sticker.Configuration;
import co.chatsdk.ui.chat.model.ImageMessageHolder;


public class StickerMessageHolder extends ImageMessageHolder {
    public StickerMessageHolder(Message message) {
        super(message);
    }

    @Nullable
    public String getImageUrl() {
        String stickerName = (String) message.valueForKey(Keys.MessageStickerName);

        Context context = ChatSDK.shared().context();
        Resources resources = context.getResources();

        int resID = Configuration.resourceId(context, stickerName);

        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(resources.getResourcePackageName(resID))
                .appendPath(resources.getResourceTypeName(resID))
                .appendPath(resources.getResourceEntryName(resID))
                .build();

        return uri.toString();
    }


}
