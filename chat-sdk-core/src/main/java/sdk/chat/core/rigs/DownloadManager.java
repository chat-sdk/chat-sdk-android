package sdk.chat.core.rigs;


import static android.content.Context.DOWNLOAD_SERVICE;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;

import org.pmw.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.storage.TransferManager;
import sdk.chat.core.storage.TransferStatus;


public class DownloadManager extends TransferManager {

    protected Context context;
    protected FileManager fileManager;

    protected Map<String, MessageDownloadListener> listeners = new ConcurrentHashMap<>();

    public DownloadManager(Context context) {
        this.context = context;
        fileManager = new FileManager(context);
    }

    public void download(Message message, String key, String url, String name) throws IOException {

        // Make a new file
        File dir = fileManager.downloadsDirectory();
        File file = new File(dir, name);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                Logger.info("File not created");
            }
        }

        // Make a new cached file
        CachedFile cf = add(file, message.getEntityID(), key, url, CachedFile.Type.Download, true);

        MessageDownloadListener listener = new MessageDownloadListener(message, cf);
        listeners.put(message.getEntityID(), listener);

        AndroidNetworking.download(url, dir.getPath(), file.getName())
                .setTag(message.getEntityID())
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(listener)
                .startDownload(listener);

    }

    public void downloadInBackground(String url, String name) {

        Uri uri = Uri.parse(url);
        String ext = MimeTypeMap.getFileExtensionFromUrl(url);

        name = name + System.currentTimeMillis() + "." + ext;

        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(uri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, name);
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // to notify when download is complete
        request.allowScanningByMediaScanner();// if you want to be available from media players
        android.app.DownloadManager manager = (android.app.DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        manager.enqueue(request);

    }

    public TransferStatus getDownloadStatus(Message message) {
        MessageDownloadListener listener = listeners.get(message.getEntityID());
        if (listener != null) {
            return listener.getStatus();
        }
        return TransferStatus.None;
    }

    public ANError getDownloadError(Message message) {
        MessageDownloadListener listener = listeners.get(message.getEntityID());
        if (listener != null) {
            return listener.getError();
        }
        return null;
    }

}
