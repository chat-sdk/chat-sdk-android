package sdk.chat.message.audio;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.AbstractMessagePayload;
import sdk.chat.core.session.ChatSDK;

public class AudioMessagePayload extends AbstractMessagePayload {

    public AudioMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return message.stringForKey(Keys.MessageAudioURL);
    }

    @Override
    public String imageURL() {
        return null;
    }

//    @Override
//    public List<String> remoteURLs() {
//        List<String> urls = new ArrayList<>();
//        String audioURL = message.stringForKey(Keys.MessageAudioURL);
//        if (audioURL != null) {
//            urls.add(audioURL);
//        }
//        return urls;
//    }
//
//    @Override
//    public Completable downloadMessageContent() {
//        return Completable.create(emitter -> {
//            // TODO:
//        });
//    }

    @Override
    public String lastMessageText() {
        return ChatSDK.getString(R.string.audio_message);
    }
}
