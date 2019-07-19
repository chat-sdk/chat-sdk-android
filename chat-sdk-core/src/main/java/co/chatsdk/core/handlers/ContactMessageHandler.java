package co.chatsdk.core.handlers;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import io.reactivex.Completable;

public interface ContactMessageHandler extends MessageHandler {
    Completable sendMessageWithContact(User contact, final Thread thread);
}
