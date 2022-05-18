package sdk.chat.core.storage;

import java.util.List;

import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.session.ChatSDK;

public class TransferManager {

    public CachedFile getFile(String hash) {
        return ChatSDK.db().fetchEntityWithEntityID(hash, CachedFile.class);
    }

    public List<CachedFile> getFiles(String identifier) {
        return ChatSDK.db().fetchFilesWithIdentifier(identifier);
    }

}
