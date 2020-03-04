package co.chatsdk.core.audio;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.offline.DefaultDownloadIndex;
import com.google.android.exoplayer2.offline.DefaultDownloaderFactory;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.offline.DownloaderConstructorHelper;
import com.google.android.exoplayer2.scheduler.Scheduler;
import com.google.android.exoplayer2.ui.DownloadNotificationHelper;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.util.List;

import co.chatsdk.core.R;
import co.chatsdk.core.session.ChatSDK;

// TODO: Not finished...
public class Audio {

//    private static final String DOWNLOAD_ACTION_FILE = "actions";
//    private static final String DOWNLOAD_TRACKER_ACTION_FILE = "tracked_actions";
    private static final String DOWNLOAD_CONTENT_DIRECTORY = "downloads";
    public static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel";

    protected Context context;

    ExoDatabaseProvider databaseProvider;
    SimpleCache downloadCache;
    DownloadManager downloadManager;
    protected File downloadDirectory;
    protected DownloadNotificationHelper downloadNotificationHelper;



    public static final Audio instance = new Audio();

    public static Audio shared() {
        return instance;
    }

    private File getDownloadDirectory() {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir(null);
            if (downloadDirectory == null) {
                downloadDirectory = context.getFilesDir();
            }
        }
        return downloadDirectory;
    }

    public Audio() {

        databaseProvider = new ExoDatabaseProvider(ChatSDK.shared().context());

        File downloadContentDirectory = new File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY);

        downloadCache = new SimpleCache(downloadContentDirectory, new NoOpCacheEvictor(), databaseProvider);

        DefaultDownloadIndex downloadIndex = new DefaultDownloadIndex(databaseProvider);

        DownloaderConstructorHelper downloaderConstructorHelper =
                new DownloaderConstructorHelper(downloadCache, new DefaultHttpDataSourceFactory(context.getString(R.string.app_name)));

        downloadNotificationHelper = new DownloadNotificationHelper(context, DOWNLOAD_NOTIFICATION_CHANNEL_ID);

        downloadManager = new DownloadManager(context, downloadIndex, new DefaultDownloaderFactory(downloaderConstructorHelper));

    }

    public void startDownloadService(Context context, File downloadDirectory, String userAgent) {

//        databaseProvider = new ExoDatabaseProvider(context);
//
//// A download cache should not evict media, so should use a NoopCacheEvictor.
//        downloadCache = new SimpleCache(
//                downloadDirectory,
//                new NoOpCacheEvictor(),
//                databaseProvider);
//
//// Create a factory for reading the data from the network.
//        dataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);

// Create the download manager.
//        downloadManager = new DownloadManager(
//                context,
//                databaseProvider,
//                downloadCache,
//                dataSourceFactory);
//
//// Optionally, setters can be called to configure the download manager.
//        downloadManager.setRequirements(requirements);
//        downloadManager.setMaxParallelDownloads(3);

    }


}
