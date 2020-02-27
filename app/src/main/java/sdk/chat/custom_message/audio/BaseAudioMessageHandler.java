package sdk.chat.custom_message.audio;

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


/**
 * Created by ben on 9/28/17.
 */

public class BaseAudioMessageHandler implements AudioMessageHandler {

    @Override
    public Completable sendMessage(final Recording recording, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Audio), thread, null).setUploadable(new FileUploadable(recording.getFile(), recording.getName(), recording.getMimeType()), (message, result) -> {
            Integer duration = recording.getDurationMillis() / 1000;
            message.setText(ChatSDK.shared().context().getString(R.string.audio_message));
            message.setValueForKey(result.url, Keys.MessageAudioURL);
            message.setValueForKey(duration, Keys.MessageAudioLength);
            message.update();
        }).run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageAudioURL);
    }

}
