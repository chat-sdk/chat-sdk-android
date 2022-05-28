package sdk.chat.message.audio;

import android.Manifest;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.session.Configure;
import sdk.chat.licensing.Report;
import sdk.chat.ui.ChatSDKUI;
import sdk.guru.common.BaseConfig;

//import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
//import cafe.adriel.androidaudioconverter.callback.ILoadCallback;

/**
 * Created by ben on 9/28/17.
 */

public class AudioMessageModule extends AbstractModule {

    public static final AudioMessageModule instance = new AudioMessageModule();

    public static AudioMessageModule shared() {
        return instance;
    }

    /**
     * @see Config
     * @return configuration object
     */
    public static Config<AudioMessageModule> builder() {
        return instance.config;
    }

    public static AudioMessageModule builder(Configure<Config> config) throws Exception {
        config.with(instance.config);
        return instance;
    }

    public static class Config<T> extends BaseConfig<T> {

//        public boolean compressionEnabled = true;
        public int minimumAudioRecordingLength = 2;

        public Config(T onBuild) {
            super(onBuild);
        }

        public Config<T> setMinimumAudioRecordingLength(int length) {
            this.minimumAudioRecordingLength = length;
            return this;
        }

        /**
         * Set audio compression enabled
         * @param compressionEnabled
         * @return
         */
//        public Config<T> setCompressionEnabled(boolean compressionEnabled) {
//            this.compressionEnabled = compressionEnabled;
//            return this;
//        }

        public boolean preferStreamAudio = false;
        public Config<T> setPreferStreamAudio(boolean value) {
            this.preferStreamAudio = value;
            return this;
        }

    }

    public static Config config() {
        return shared().config;
    }

    protected Config<AudioMessageModule> config = new Config<>(this);

    @Override
    public void activate(Context context) {
        ChatSDK.a().audioMessage = new BaseAudioMessageHandler();
        Report.shared().add(getName());

//        AndroidAudioConverter.load(context, new ILoadCallback() {
//            @Override
//            public void onSuccess() {
//                ChatSDK.a().audioMessage.setCompressionEnabled(true);
//            }
//
//            @Override
//            public void onFailure(Exception error) {
//                ChatSDK.events().onError(error);
//            }
//        });

        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new AudioMessageRegistration());
    }

    @Override
    public MessageHandler getMessageHandler() {
        return ChatSDK.audioMessage();
    }

    public List<String> requiredPermissions() {
        List<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.RECORD_AUDIO);

        return permissions;
    }

    @Override
    public void stop() {
        config = new Config<>(this);
    }

}
