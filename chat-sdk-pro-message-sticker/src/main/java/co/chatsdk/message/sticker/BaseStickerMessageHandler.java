package co.chatsdk.message.sticker;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.StickerMessageHandler;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageType;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Created by ben on 10/11/17.
 */

public class BaseStickerMessageHandler implements StickerMessageHandler {
    @Override
    public Completable sendMessageWithSticker(final String stickerImageName, final Thread thread) {
        return new MessageSendRig(new MessageType(MessageType.Sticker), thread, message -> {
            message.setText(stickerImageName);
            message.setValueForKey(stickerImageName, Keys.MessageStickerName);
        }).run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageStickerName);
    }
}
