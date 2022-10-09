package sdk.chat.message.audio;

import android.content.Context;

import java.io.File;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.base.AbstractMessageHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.message.audio.keyboard.RecordAudioKeyboardOverlayFragment;
import sdk.guru.common.RX;

//import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
//import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
//import cafe.adriel.androidaudioconverter.model.AudioFormat;


/**
 * Created by ben on 9/28/17.
 */

public class BaseAudioMessageHandler extends AbstractMessageHandler implements AudioMessageHandler {

    protected boolean compressionEnabled = false;

    @Override
    public Completable sendMessage(Context context, final File file, String mimeType, long duration, final Thread thread) {
        return compressAudio(context, file).flatMapCompletable(file1 -> {
            return new MessageSendRig(new MessageType(MessageType.Audio), thread, message -> {
                message.setText(ChatSDK.getString(R.string.audio_message));
                message.setValueForKey(duration, Keys.MessageAudioLength);
                message.setValueForKey(file.length(), Keys.MessageSize);
                message.setFilePath(file.getPath());
                ChatSDK.db().update(message);

            }).setUploadable(new FileUploadable(file1, "recording", mimeType, Keys.MessageAudioURL), null).run();
        }).doOnError(throwable -> {

        });
    }

    @Override
    public void setCompressionEnabled(boolean enabled) {
        compressionEnabled = enabled;
    }

    @Override
    public AbstractKeyboardOverlayFragment getOverlay(KeyboardOverlayHandler handler) {
        AbstractKeyboardOverlayFragment fragment = ChatSDK.feather().instance(RecordAudioKeyboardOverlayFragment.class);
        fragment.setHandler(handler);
        return fragment;
    }

    public Single<File> compressAudio(Context context, File audioFile) {
        return Single.defer(() -> {
//            if (!compressionEnabled || !AudioMessageModule.config().compressionEnabled) {
                return Single.just(audioFile);
//            }
//            return Single.create(emitter -> {
//                AndroidAudioConverter.with(context).setFile(audioFile).setFormat(AudioFormat.MP3).setCallback(new IConvertCallback() {
//                    @Override
//                    public void onSuccess(File convertedFile) {
//                        emitter.onSuccess(convertedFile);
//                    }
//
//                    @Override
//                    public void onFailure(Exception error) {
//                        emitter.onError(error);
//                    }
//                }).convert();
//            });
        }).subscribeOn(RX.computation());
    }

    @Override
    public MessagePayload payloadFor(Message message) {
        return new AudioMessagePayload(message);
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.Audio);
    }

}
