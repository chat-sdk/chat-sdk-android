package sdk.chat.audio;

import android.content.Context;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import co.chatsdk.message.audio.R;
import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.guru.common.RX;


/**
 * Created by ben on 9/28/17.
 */

public class BaseAudioMessageHandler implements AudioMessageHandler {

    protected boolean compressionEnabled = false;

    @Override
    public Completable sendMessage(Context context, final File file, String mimeType, long duration, final Thread thread) {
        return compressAudio(context, file).flatMapCompletable(file1 -> {
            return new MessageSendRig(new MessageType(MessageType.Audio), thread, null).setUploadable(new FileUploadable(file1, "recording", mimeType), (message, result) -> {
                message.setText(ChatSDK.getString(R.string.audio_message));
                message.setValueForKey(result.url, Keys.MessageAudioURL);
                message.setValueForKey(duration, Keys.MessageAudioLength);
                message.update();
            }).run();
        });
    }

    @Override
    public void setCompressionEnabled(boolean enabled) {
        compressionEnabled = enabled;
    }

    public Single<File> compressAudio(Context context, File audioFile) {
        return Single.defer(() -> {
            if (!compressionEnabled) {
                return Single.just(audioFile);
            }
            return Single.create(emitter -> {
                AndroidAudioConverter.with(context).setFile(audioFile).setFormat(AudioFormat.MP3).setCallback(new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {
                        emitter.onSuccess(convertedFile);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        emitter.onError(error);
                    }
                }).convert();
            });
        }).subscribeOn(RX.computation());
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageAudioURL);
    }

}
