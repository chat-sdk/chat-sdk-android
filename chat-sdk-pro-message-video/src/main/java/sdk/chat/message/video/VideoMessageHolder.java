package sdk.chat.message.video;

import android.content.Context;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import io.reactivex.Single;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.model.ImageMessageHolder;

public class VideoMessageHolder extends ImageMessageHolder implements MessageContentType {

    public VideoMessageHolder(Message message) {
        super(message);
    }

    public String getImageUrl() {
        if (payload != null) {
            return payload.imageURL();
        }
        return null;
    }

    public String getVideoURL() {
        return (String) message.valueForKey(Keys.MessageVideoURL);
    }

    @Override
    public Single<String> save(Context context) {
        return Single.create(emitter -> {
            ChatSDK.downloadManager().downloadInBackground(getVideoURL(), "Video");
            emitter.onSuccess(context.getString(R.string.downloading_to_downloads_folder));
        });
    }

    public boolean canSave() {
        return true;
    }

}
