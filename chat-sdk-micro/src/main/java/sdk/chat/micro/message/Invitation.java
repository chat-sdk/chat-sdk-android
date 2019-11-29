package sdk.chat.micro.message;

import java.security.acl.Group;

import sdk.chat.micro.types.InvitationType;
import sdk.chat.micro.types.SendableType;

public class Invitation extends Sendable {

    public static String GroupUid = "gid";

    public Invitation() {
        type = SendableType.Invitation;
    }

    public Invitation(InvitationType type, String groupId) {
        this();
        super.setType(type);
        body.put(GroupUid, groupId);
    }

    public InvitationType getBodyType() {
        return new InvitationType(super.getBodyType());
    }

    public String getGroupUid() throws Exception {
        return getBodyString(GroupUid);
    }

}
