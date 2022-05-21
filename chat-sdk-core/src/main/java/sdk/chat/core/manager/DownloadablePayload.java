package sdk.chat.core.manager;

import io.reactivex.Completable;
import sdk.chat.core.storage.TransferStatus;

public interface DownloadablePayload {

    TransferStatus downloadStatus();
    boolean canDownload();
    Completable startDownload();
    Integer size();

}
