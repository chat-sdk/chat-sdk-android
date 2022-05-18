package sdk.chat.core.rigs;


import static android.content.Context.DOWNLOAD_SERVICE;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;

import java.io.File;
import java.util.List;
import java.util.UUID;

import io.reactivex.Single;
import sdk.chat.core.dao.CachedFile;
import sdk.chat.core.dao.Message;
import sdk.chat.core.storage.FileManager;
import sdk.chat.core.storage.TransferManager;


public class DownloadManager extends TransferManager {

    protected Context context;
    protected FileManager fileManager;

    public DownloadManager(Context context) {
        this.context = context;
        fileManager = new FileManager(context);
    }

    public String download(String url, File toDir, String name, DownloadProgressListener progressListener, DownloadListener completion) {

        // Make a new file
        File file = fileManager.downloadsDirectory();

        String token = UUID.randomUUID().toString();
        AndroidNetworking.download(url, toDir.getPath(), name)
                .setTag(token)
                .setPriority(Priority.MEDIUM)
                .build()
                .setDownloadProgressListener(progressListener)
                .startDownload(completion);

        return token;
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

    public Single<CachedFile> download(Message message, String remoteURL) {
        return Single.create(emitter -> {
            List<CachedFile> cachedFiles = getFiles(message.getEntityID());
            CachedFile cf = null;
            for (CachedFile file: cachedFiles) {
                if (file.getRemotePath() != null && file.getRemotePath().equals(remoteURL)) {
                    cf = file;
                    break;
                }
            }
            // This is our file
            if (cf == null) {


//                AndroidNetworking.download(url, toDir.getPath(), name)
//                        .setTag(token)
//                        .setPriority(Priority.MEDIUM)
//                        .build()
//                        .setDownloadProgressListener(progressListener)
//                        .startDownload(completion);

            }


            emitter.onSuccess(cf);


        });
    }

}
