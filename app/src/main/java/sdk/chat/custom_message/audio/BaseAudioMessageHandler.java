package sdk.chat.custom_message.audio;

import java.io.File;
import java.util.concurrent.Callable;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import co.chatsdk.android.app.R;
import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.AudioMessageHandler;
import co.chatsdk.core.rigs.FileUploadable;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageType;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by ben on 9/28/17.
 */

public class BaseAudioMessageHandler implements AudioMessageHandler {

    @Override
    public Completable sendMessage(final File file, String mimeType, int duration, final Thread thread) {
        return compressAudio(file).flatMapCompletable(file1 -> {
            return new MessageSendRig(new MessageType(MessageType.Audio), thread, null).setUploadable(new FileUploadable(file1, "recording", mimeType), (message, result) -> {
                message.setText(ChatSDK.shared().context().getString(R.string.audio_message));
                message.setValueForKey(result.url, Keys.MessageAudioURL);
                message.setValueForKey(duration, Keys.MessageAudioLength);
                message.update();
            }).run();
        });
    }

    public Single<File> compressAudio(File audioFile) {
        return Single.create((SingleOnSubscribe<File>) emitter -> {
            AndroidAudioConverter.with(ChatSDK.shared().context()).setFile(audioFile).setFormat(AudioFormat.MP3).setCallback(new IConvertCallback() {
                @Override
                public void onSuccess(File convertedFile) {
                    emitter.onSuccess(convertedFile);
                }

                @Override
                public void onFailure(Exception error) {
                    emitter.onError(error);
                }
            });
        }).subscribeOn(Schedulers.single());
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageAudioURL);
    }

}
