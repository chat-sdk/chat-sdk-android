package sdk.chat.message.video;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import co.chatsdk.ui.chat.model.ImageMessageHolder;

public class VideoMessageHolder extends ImageMessageHolder implements MessageContentType {
    public VideoMessageHolder(Message message) {
        super(message);
    }

    public String getImageUrl() {
        return ChatSDK.videoMessage().getImageURL(message);
    }

    @Override
    public String getText() {
        return ChatSDK.videoMessage().toString(message);
    }

}
