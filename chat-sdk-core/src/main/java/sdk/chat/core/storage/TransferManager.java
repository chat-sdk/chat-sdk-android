package sdk.chat.core.storage;

import android.util.Base64;

import java.io.File;
import java.security.MessageDigest;
import java.util.Date;
import java.util.List;

import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.session.ChatSDK;

public class TransferManager {

    public CachedFile getFile(String hash, String messageEntityID) {
        return ChatSDK.db().fetchCachedFileWithHash(hash, messageEntityID);
//        return ChatSDK.db().fetchEntityWithEntityID(hash, CachedFile.class);
    }

    public List<CachedFile> getFiles(String identifier) {
        return ChatSDK.db().fetchFilesWithIdentifier(identifier);
    }

    public CachedFile add(File file, String identifier, String key, String remotePath, CachedFile.Type type, boolean reportProgress) {
        // See if this uploadable already exists
        String hash = TransferManager.hash(file);
        if (hash != null) {
            CachedFile cf = ChatSDK.db().fetchOrCreateCachedFileWithHash(hash, identifier);
            cf.setLocalPath(file.getPath());
            cf.setRemotePath(remotePath);
            cf.setIdentifier(identifier);
            cf.setName(file.getName());
            cf.setTransferStatus(TransferStatus.Initial);
            cf.setFileType(type);
            cf.setMessageKey(key);
            cf.setReportProgress(reportProgress);
            cf.setStartTime(new Date());
            ChatSDK.db().update(cf);
            return cf;
        }
        return null;
    }
    public static String hash(byte [] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            String hashString = Base64.encodeToString(hash, Base64.DEFAULT);
            hashString = hashString.replace("=", "_");
            hashString = hashString.replace("+", "-");
            hashString = hashString.replace("/", "__");
            return hashString;
        } catch (Exception e) {
            return null;
        }
    }

    public static String hash(File file) {
        return hash(FileManager.fileToBytes(file));
    }

}
