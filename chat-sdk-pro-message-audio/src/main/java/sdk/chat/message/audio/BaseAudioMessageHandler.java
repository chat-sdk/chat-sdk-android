package sdk.chat.message.audio;

import android.content.Context;

import java.io.File;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import sdk.chat.core.base.AbstractMessageHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.AudioMessageHandler;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.guru.common.RX;


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
                message.update();

            }).setUploadable(new FileUploadable(file1, "recording", mimeType, Keys.MessageAudioURL), (message, result) -> {
//                message.setValueForKey(result.url, Keys.MessageAudioURL);
//                message.setValueForKey(duration, Keys.MessageAudioLength);
//                message.update();
            }).run();
        });
    }

    @Override
    public void setCompressionEnabled(boolean enabled) {
        compressionEnabled = enabled;
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
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageAudioURL);
    }

    @Override
    public String toString(Message message) {
        return ChatSDK.getString(R.string.audio_message);
    }

    @Override
    public String getImageURL(Message message) {
//        if (message.getMessageType().is(MessageType.Audio) || message.getReplyType().is(MessageType.Audio)) {
//            return ImageUtils.uriForResourceId(ChatSDK.ctx(), R.drawable.icn_50_audio).toString();
//        }
        return null;
    }

    @Override
    public List<String> remoteURLs(Message message) {
        List<String> urls = super.remoteURLs(message);
        if (!message.typeIs(MessageType.Audio)) {
            return urls;
        }
        String audioURL = message.stringForKey(Keys.MessageAudioURL);
        if (audioURL != null) {
            urls.add(audioURL);
        }
        return urls;
    }

}
