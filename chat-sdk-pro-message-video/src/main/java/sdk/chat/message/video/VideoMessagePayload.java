package sdk.chat.message.video;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.AbstractMessagePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.utils.StringChecker;

public class VideoMessagePayload extends AbstractMessagePayload {

    public VideoMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return message.stringForKey(Keys.MessageVideoURL);
    }

    @Override
    public String imageURL() {
        if (message.getMessageType().is(MessageType.Video) || message.getReplyType().is(MessageType.Video)) {
            return message.getImageURL();
        }
        return null;
    }

    @Override
    public List<String> remoteURLs() {
        List<String> urls = new ArrayList<>();
        String imageURL = message.stringForKey(Keys.MessageImageURL);
        if (imageURL != null) {
            urls.add(imageURL);
        }
        String videoURL = message.stringForKey(Keys.MessageVideoURL);
        if (videoURL != null) {
            urls.add(videoURL);
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
            text = ChatSDK.getString(R.string.video_message);
        }
        return text;
    }
}
