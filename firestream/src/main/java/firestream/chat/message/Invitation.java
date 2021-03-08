package firestream.chat.message;

import firestream.chat.chat.Chat;
import firestream.chat.namespace.Fire;
import io.reactivex.Completable;

import firestream.chat.types.InvitationType;
import firestream.chat.types.SendableType;

public class Invitation extends Sendable {

    public static String ChatId = "id";

    public Invitation() {
        type = SendableType.Invitation;
    }

    public Invitation(InvitationType type, String chatId) {
        this();
        super.setBodyType(type);
        body.put(ChatId, chatId);
    }

    public InvitationType getBodyType() {
        return new InvitationType(super.getBodyType());
    }

    public String getChatId() {
        return getBodyString(ChatId);
    }

    public Completable accept() {
        if (getBodyType().equals(InvitationType.chat())) {
            try {
                return Fire.stream().joinChat(new Chat(getChatId()));
            } catch (Exception e) {
                return Completable.error(e);
            }
        }
        return Completable.complete();
    }

    public static Invitation fromSendable(Sendable sendable) {
        Invitation invitation = new Invitation();
        sendable.copyTo(invitation);
        return invitation;
    }

}
