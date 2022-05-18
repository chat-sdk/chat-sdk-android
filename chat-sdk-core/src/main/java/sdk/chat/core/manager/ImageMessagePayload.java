package sdk.chat.core.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.R;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;
import sdk.chat.core.utils.StringChecker;

public class ImageMessagePayload extends AbstractMessagePayload {

    public ImageMessagePayload(Message message) {
        super(message);
    }

    @Override
    public String getText() {
        return message.stringForKey(Keys.MessageImageURL);
    }

    @Override
    public String imageURL() {
//        if (message.getMessageType().is(MessageType.Image) || message.getReplyType().is(MessageType.Image)) {
//            return message.getImageURL();
//        }
        return message.getImageURL();
    }

    @Override
    public List<String> remoteURLs() {
        List<String> urls = new ArrayList<>();
        String url = message.stringForKey(Keys.MessageImageURL);
        if (url != null) {
            urls.add(url);
        }
        return urls;
    }

    @Override
    public Completable downloadMessageContent() {
        return Completable.create(emitter -> {
            // In this case we only download the placeholder
            if (message.getPlaceholderPath() == null) {
                // Check to see if the image remote path exists
                String remoteURL = message.stringForKey(Keys.MessageImageURL);

                if (remoteURL != null) {
                    ChatSDK.downloadManager().download(message, remoteURL);
                }

                // Make a new file
                CachedFile cf = ChatSDK.db().createEntity(CachedFile.class);

                cf.setIdentifier(message.getEntityID());
                cf.setTransferStatus(TransferStatus.WillStart);
                cf.setFileType(CachedFile.Type.Download);

                cf.setMessageKey(Keys.MessageImageURL);
                cf.setReportProgress(true);
                cf.setStartTime(new Date());
                cf.update();
                //
            } else {
                emitter.onComplete();
            }
        });
    }

    @Override
    public String previewText() {
        String text = super.toString();
        if (StringChecker.isNullOrEmpty(text)) {
            text = ChatSDK.getString(R.string.image_message);
        }
        return text;
    }

}
