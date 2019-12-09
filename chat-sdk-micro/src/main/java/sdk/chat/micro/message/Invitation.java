package sdk.chat.micro.message;

import com.google.firebase.firestore.Exclude;

import java.security.acl.Group;

import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.SendableType;

public class Invitation extends Sendable {

    public static String GroupUid = "id";

    public Invitation() {
        type = SendableType.Invitation;
    }

    public Invitation(InvitationType type, String groupId) {
        this();
        super.setBodyType(type);
        body.put(GroupUid, groupId);
    }

    @Exclude
    public InvitationType getBodyType() {
        return new InvitationType(super.getBodyType());
    }

    @Exclude
    public String getGroupUid() throws Exception {
        return getBodyString(GroupUid);
    }

}
