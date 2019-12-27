package firefly.sdk.chat.message;

import com.google.firebase.firestore.Exclude;

import io.reactivex.Completable;
import firefly.sdk.chat.namespace.Fl;
import firefly.sdk.chat.types.InvitationType;
import firefly.sdk.chat.types.SendableType;

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

    @Exclude
    public InvitationType getBodyType() {
        return new InvitationType(super.getBodyType());
    }

    @Exclude
    public String getChatId() throws Exception {
        return getBodyString(ChatId);
    }

    public Completable accept() {
        if (getBodyType().equals(InvitationType.chat())) {
            try {
                return Fl.y.joinChat(getChatId());
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
