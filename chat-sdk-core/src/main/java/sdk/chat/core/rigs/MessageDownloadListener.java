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

    public MessageDownloadListener(Message message, CachedFile cachedFile) {
        this.message = message;
        this.cachedFile = cachedFile;
    }

    @Override
    public void onDownloadComplete() {
        message.setFilePath(cachedFile.getLocalPath());
        message.update();

        cachedFile.setTransferStatus(TransferStatus.Complete);
        cachedFile.setFinishTime(new Date());
        cachedFile.update();

        status = TransferStatus.Complete;
        ChatSDK.events().source().accept(NetworkEvent.messageSendStatusChanged(message));
    }

    @Override
    public void onError(ANError anError) {
        cachedFile.setTransferStatus(TransferStatus.Failed);
        cachedFile.update();
        error = anError;
        status = TransferStatus.Failed;
        ChatSDK.events().source().accept(NetworkEvent.messageProgressUpdated(message, new Progress(error)));
    }

    @Override
    public void onProgress(long transferred, long total) {
        status = TransferStatus.InProgress;
        cachedFile.setTransferStatus(TransferStatus.InProgress);
        cachedFile.update();
        ChatSDK.events().source().accept(NetworkEvent.messageProgressUpdated(message, new Progress(transferred, total)));
        ChatSDK.events().source().accept(NetworkEvent.messageSendStatusChanged(message));
    }

    public TransferStatus getStatus() {
        return status;
    }

    public ANError getError() {
        return error;
    }

}
