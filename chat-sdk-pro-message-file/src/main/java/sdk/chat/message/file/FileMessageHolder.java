package sdk.chat.message.file;

import android.content.Context;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import io.reactivex.Single;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.chat.model.MessageHolder;

public class FileMessageHolder extends MessageHolder implements MessageContentType {

    public FileMessageHolder(Message message) {
        super(message);
    }

    public String getIcon() {
        if (payload != null) {
            return payload.imageURL();
        }
        return null;
    }

    public String getFileURL() {
        return (String) message.valueForKey(Keys.MessageFileURL);
    }

    @Override
    public Single<String> save(Context context) {
        return Single.create(emitter -> {
            ChatSDK.downloadManager().downloadInBackground(getFileURL(), "File");
            emitter.onSuccess(context.getString(sdk.chat.core.R.string.downloading_to_downloads_folder));
        });
    }

    public boolean canSave() {
        return true;
    }

    public boolean enableLinkify() {
        return false;
    }

}
