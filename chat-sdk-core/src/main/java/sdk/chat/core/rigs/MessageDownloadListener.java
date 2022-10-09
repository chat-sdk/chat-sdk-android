package sdk.chat.core.rigs;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadProgressListener;

import java.util.Date;

import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.storage.TransferStatus;
import sdk.chat.core.types.Progress;

public class MessageDownloadListener implements DownloadProgressListener, com.androidnetworking.interfaces.DownloadListener {

    protected Message message;
    protected CachedFile cachedFile;
    protected TransferStatus status = TransferStatus.None;
    protected ANError error;

    protected Progress lastProgress = null;
    protected TransferStatus lastStatus = null;

    public MessageDownloadListener(Message message, CachedFile cachedFile) {
        this.message = message;
        this.cachedFile = cachedFile;
    }

    @Override
    public void onDownloadComplete() {
        message.setFilePath(cachedFile.getLocalPath());
        ChatSDK.db().update(message);

        cachedFile.setFinishTime(new Date());

        updateStatus(TransferStatus.Complete);
    }

    @Override
    public void onError(ANError anError) {
        updateStatus(TransferStatus.Failed);
        error = anError;
        ChatSDK.events().source().accept(NetworkEvent.messageProgressUpdated(message, new Progress(error)));
    }

    @Override
    public void onProgress(long transferred, long total) {
        updateStatus(TransferStatus.InProgress);

        Progress progress = new Progress(transferred, total);
        if (lastProgress == null || progress.asPercentage() - lastProgress.asPercentage() > 5) {
            ChatSDK.events().source().accept(NetworkEvent.messageProgressUpdated(message, new Progress(transferred, total)));
            lastProgress = progress;
        }

    }

    public void updateStatus(TransferStatus value) {
        if (status != value) {
            status = value;
            cachedFile.setTransferStatus(status);
            ChatSDK.db().update(cachedFile);
            ChatSDK.events().source().accept(NetworkEvent.messageSendStatusChanged(message));
        }
    }

    public TransferStatus getStatus() {
        return status;
    }

    public ANError getError() {
        return error;
    }

}
