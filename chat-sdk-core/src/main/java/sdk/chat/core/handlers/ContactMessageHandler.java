package sdk.chat.core.handlers;

import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import io.reactivex.Completable;

public interface ContactMessageHandler extends MessageHandler {
    Completable sendMessageWithContact(User contact, final Thread thread);
}
