package sdk.chat.core.storage;

import java.util.Date;
import java.util.List;

import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.session.ChatSDK;

public class UploadManager {

    public CachedFile add(Uploadable uploadable, Message message) {
        // See if this uploadable already exists
        String hash = uploadable.hash();
        if (hash != null) {
            CachedFile file = ChatSDK.db().fetchOrCreateEntityWithEntityID(CachedFile.class, hash);
            String path = uploadable.cache();
            file.setLocalPath(path);
            file.setIdentifier(message.getEntityID());
            file.setMimeType(uploadable.mimeType);
            file.setName(uploadable.name);
            file.setUploadStatus(UploadStatus.WillStart);
            file.setFileType(CachedFile.Type.Upload);
            file.setMessageKey(uploadable.messageKey);
            file.setReportProgress(uploadable.reportProgress);
            file.setStartTime(new Date());
            file.update();
            return file;
        }
        return null;
    }

    public CachedFile getFile(String hash) {
        return ChatSDK.db().fetchEntityWithEntityID(hash, CachedFile.class);
    }

    public boolean setStatus(String hash, UploadStatus status) {
        CachedFile file = getFile(hash);
        if (file != null) {
            file.setUploadStatus(status);
            file.update();
            return true;
        }
        return false;
    }

    public List<CachedFile> getFiles(String identifier) {
        return ChatSDK.db().fetchFilesWithIdentifier(identifier);
    }

}
