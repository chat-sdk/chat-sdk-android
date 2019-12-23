package sdk.chat.micro.message;

import com.google.firebase.firestore.Exclude;

import io.reactivex.Completable;
import sdk.chat.micro.Fireflyy;
import sdk.chat.micro.namespace.Fire;
import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.SendableType;

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
                return Fire.flyy.joinChat(getChatId());
            } catch (Exception e) {
                return Completable.error(e);
            }
        }
        return Completable.complete();
    }

}
