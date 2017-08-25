package co.chatsdk.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.NM;
import co.chatsdk.core.StorageManager;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.utils.AppContext;

/**
 * Created by ben on 8/16/17.
 */

public class XMPPTypingIndicatorManager {

    // A map in the form:
    // {
    //    threadID: {userID: userName}
    // }
    private HashMap<String, HashMap<String, String>> typing = new HashMap<>();

    public void handleMessage (Message message, User user) {

        ChatStateExtension extension = (ChatStateExtension) message.getExtension(ChatStateExtension.NAMESPACE);
        ChatState state = extension.getChatState();

        Thread thread = StorageManager.shared().fetchThreadWithEntityID(message.getFrom().asBareJid().toString());
        if(user != null && !user.equals(NM.currentUser()) && thread != null) {
            setTyping(thread, user, state.equals(ChatState.composing));
        }

        NM.events().source().onNext(NetworkEvent.typingStateChanged(notificationForThread(thread), thread));
    }

    private void setTyping (Thread thread, User user, boolean isTyping) {
        HashMap map = typing.get(thread.getEntityID());
        if(map == null) {
            map = new HashMap();
            typing.put(thread.getEntityID(), map);
        }
        if(isTyping) {
            map.put(user.getEntityID(), user.getName());
        }
        else {
            map.remove(user.getEntityID());
        }
    }

    private String notificationForThread (Thread thread) {
        Map<String, String> map = typing.get(thread.getEntityID());
        if(map == null || map.keySet().size() == 0) {
            return null;
        }
        if(thread.typeIs(ThreadType.Private1to1)) {
            return "";
        }

        String message = "";
        for (String key : map.keySet()) {
            message += map.get(key) + ", ";
        }
        if(message.length() >= 2) {
            message = message.substring(0, message.length() - 2);
        }
        return message;
    }

}
