package sdk.chat.message.video;

import com.stfalcon.chatkit.commons.models.MessageContentType;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.chat.model.ImageMessageHolder;

public class VideoMessageHolder extends ImageMessageHolder implements MessageContentType {
    public VideoMessageHolder(Message message) {
        super(message);
    }
    public String getImageUrl() {
        return message.imageURL();
    }

    @Override
    public String getText() {
        return ChatSDK.shared().getString(co.chatsdk.ui.R.string.video_message);
    }

}
