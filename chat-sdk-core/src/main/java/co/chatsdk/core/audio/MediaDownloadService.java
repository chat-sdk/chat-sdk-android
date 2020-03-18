package co.chatsdk.core.audio;

import android.app.Notification;
import android.os.Build;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadService;
import com.google.android.exoplayer2.scheduler.PlatformScheduler;
import com.google.android.exoplayer2.scheduler.Scheduler;

import java.util.List;

import co.chatsdk.core.R;

public class MediaDownloadService extends DownloadService {

    private static final int JOB_ID = 1;
    private static final int FOREGROUND_NOTIFICATION_ID = 1;
    public static final String DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel";

    public MediaDownloadService() {
        super(FOREGROUND_NOTIFICATION_ID,
                DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
                DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                R.string.exo_download_notification_channel_name,
                0);
    }

    @Override
    protected DownloadManager getDownloadManager() {
        return Audio.shared().downloadManager;
    }

    @Nullable
    @Override
    protected Scheduler getScheduler() {
        return Build.VERSION.SDK_INT >= 21 ? new PlatformScheduler(this, JOB_ID) : null;
    }

    @Override
    protected Notification getForegroundNotification(List<Download> downloads) {
        return null;
//        return Audio.shared().downloadNotificationHelper.buildProgressNotification();
    }
}
