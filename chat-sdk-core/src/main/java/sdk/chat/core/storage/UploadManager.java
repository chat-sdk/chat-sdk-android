package sdk.chat.core.storage;

import java.util.Date;

import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.session.ChatSDK;

public class UploadManager extends TransferManager {

    public CachedFile add(Uploadable uploadable, Message message) {
        // See if this uploadable already exists
        String hash = uploadable.hash();

        if (hash != null) {
//            CachedFile file = ChatSDK.db().fetchOrCreateEntityWithEntityID(CachedFile.class, hash);
            CachedFile file = ChatSDK.db().fetchOrCreateCachedFileWithHash(hash, message.getEntityID());
            String path = uploadable.cache();
            file.setLocalPath(path);
            file.setIdentifier(message.getEntityID());
            file.setMimeType(uploadable.mimeType);
            file.setName(uploadable.name);
            file.setTransferStatus(TransferStatus.Initial);
            file.setFileType(CachedFile.Type.Upload);
            file.setMessageKey(uploadable.messageKey);
            file.setReportProgress(uploadable.reportProgress);
            file.setStartTime(new Date());
            ChatSDK.db().update(file);

            return file;
        }
        return null;
    }

    public boolean setStatus(String hash, TransferStatus status, String messageEntityID) {
        CachedFile file = getFile(hash, messageEntityID);
        if (file != null) {
            file.setTransferStatus(status);
            ChatSDK.db().update(file);
            return true;
        }
        return false;
    }

}
