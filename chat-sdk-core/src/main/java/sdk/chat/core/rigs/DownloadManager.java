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
import java.util.UUID;

import sdk.chat.core.storage.FileManager;


public class DownloadManager {

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
}
