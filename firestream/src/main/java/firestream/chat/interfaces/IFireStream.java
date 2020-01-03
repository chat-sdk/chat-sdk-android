package firestream.chat.interfaces;

import androidx.annotation.Nullable;

import java.util.HashMap;

import firestream.chat.chat.Chat;
import firestream.chat.message.Message;
import firestream.chat.message.Sendable;
import firestream.chat.types.DeliveryReceiptType;
import firestream.chat.types.TypingStateType;
import io.reactivex.Completable;
import io.reactivex.functions.Consumer;

public interface IFireStream extends IAbstractChat {

    /**
     * Leave the chat. When you leave, you will be removed from the
     * chat's roster
     * @param chat to leave
     * @return completion
     */
    Completable leaveChat(IChat chat);

    /**
     * Join the chat. To join you must already be in the chat roster
     * @param chat to join
     * @return completion
     */
    Completable joinChat(IChat chat);
}
