package sdk.chat.message.audio;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.manager.AbstractMessagePayload;
import sdk.chat.core.manager.DownloadablePayload;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;

public class AudioMessagePayload extends AbstractMessagePayload implements DownloadablePayload {

    public AudioMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return audioURL();
    }

    public String audioURL() {
        return message.stringForKey(Keys.MessageAudioURL);
    }

    @Override
    public String imageURL() {
        return null;
    }

    @Override
    public String lastMessageText() {
        return ChatSDK.getString(sdk.chat.core.R.string.audio_message);
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
        return downloadStatus() == TransferStatus.None && audioURL() != null;
    }

    @Override
    public Completable startDownload() {
        return Completable.create(emitter -> {
            if (canDownload()) {
                ChatSDK.downloadManager().download(message, Keys.MessageAudioURL, audioURL(), "Audio_" + message.getEntityID());
                emitter.onComplete();
            } else {
                emitter.onError(new Throwable(ChatSDK.getString(sdk.chat.core.R.string.download_failed)));
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
