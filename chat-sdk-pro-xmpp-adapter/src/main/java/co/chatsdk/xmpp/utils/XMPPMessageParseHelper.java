package co.chatsdk.xmpp.utils;

import org.jivesoftware.smack.packet.Message;

import java.util.Date;

import sdk.chat.core.dao.DaoCore;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.User;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;

public class XMPPMessageParseHelper {

    public static Thread getThread(Message message) {

        // There are three options:
        // 1-to-1 message incoming - from other user, to me
        // 1-to-1 message outcoming - from me to other user
        // Group chat = from group chat to me
        if (message.getType() == Message.Type.groupchat) {
            return getThreadForMUC(message);
        } else {
            return getThreadFor1to1(message);
        }
    }

    public static Thread getThreadForMUC(Message message) {
        // The MUC thread should already exist
        return ChatSDK.db().fetchThreadWithEntityID(XMPPMessageWrapper.with(message).getThreadEntityID());
    }

    public static Thread getThreadFor1to1(Message message) {

        XMPPMessageWrapper xmr = XMPPMessageWrapper.with(message);

        String threadID = xmr.getThreadEntityID();

        User currentUser = ChatSDK.currentUser();

        // Set the thread
        Thread thread = ChatSDK.db().fetchThreadWithEntityID(threadID);
        if(thread == null) {
            thread = DaoCore.getEntityForClass(Thread.class);
            DaoCore.createEntity(thread);
            thread.setEntityID(threadID);
            thread.setType(ThreadType.Private1to1);
            thread.setCreationDate(new Date());

            // The thread ID will always be the ID of the other user
            User otherUser = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, threadID);
            ChatSDK.core().userOn(otherUser).subscribe(ChatSDK.events());

            if (currentUser.getEntityID().equals(threadID)) {
                thread.setCreator(currentUser);
            } else {
                thread.setCreator(otherUser);
            }

            thread.addUsers(currentUser, otherUser);
        }
        return thread;
    }

}
