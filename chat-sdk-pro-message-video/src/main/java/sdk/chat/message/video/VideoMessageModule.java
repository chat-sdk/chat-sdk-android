package sdk.chat.message.video;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.options.MediaChatOption;
import sdk.chat.ui.chat.options.MediaType;
import sdk.guru.common.BaseConfig;

/**
 * Created by ben on 10/6/17.
 */

public class VideoMessageModule extends AbstractModule {

    public static final VideoMessageModule instance = new VideoMessageModule();

    public static VideoMessageModule shared() {
        return instance;
    }

    public Config<VideoMessageModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        Report.shared().add(getName());
        ChatSDK.a().videoMessage = new BaseVideoMessageHandler();

        ChatSDK.ui().addChatOption(new MediaChatOption(R.string.take_video, R.drawable.icn_100_take_video, MediaType.takeVideo()));
        ChatSDK.ui().addChatOption(new MediaChatOption(R.string.choose_video, R.drawable.icn_100_video, MediaType.chooseVideo()));
        
        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new VideoMessageRegistration());


    }

    public static class Config<T> extends BaseConfig<T> {

        public Class<? extends Activity> videoPlayerActivity = ExoVideoActivity.class;

        public long cacheSizeMB = 100;
        public long maxFileSizeInMB = 50;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Class<? extends Activity> getVideoPlayerActivity() {
            return videoPlayerActivity;
        }

        public Config<T> setVideoPlayerActivity(Class<? extends Activity> videoPlayerActivity) {
            this.videoPlayerActivity = videoPlayerActivity;
            return this;
        }

        public long getCacheSizeMB() {
            return cacheSizeMB;
        }

        public Config<T> setCacheSizeMB(long cacheSizeMB) {
            this.cacheSizeMB = cacheSizeMB;
            return this;
        }

        public Config<T> setMaxFileSizeInMB(long max) {
            this.maxFileSizeInMB = max;
            return this;
        }

        public long maxFileSizeInBytes() {
            return maxFileSizeInMB * 1000 * 1000;
        }

    }

    public static Config<VideoMessageModule> builder() {
        return instance.config;
    }

    public static VideoMessageModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }


    @Override
    public MessageHandler getMessageHandler() {
        return ChatSDK.videoMessage();
    }

    @Override
    public List<String> requiredPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.CAMERA);
        permissions.add(Manifest.permission.RECORD_AUDIO);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        return permissions;
    }

    public static Config<VideoMessageModule> config() {
        return shared().config;
    }

    @Override
    public void stop() {
    }

}
