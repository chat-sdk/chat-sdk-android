package co.chatsdk.core.hook;

import java.util.HashMap;

public class HookEvent {

    public static String DidAuthenticate = "DidAuthenticate";

    public static String UserOn = "UserOn";

    public static String MessageReceived = "MessageReceived";
    public static String IsNew_Boolean = "IsNew_Boolean";

    public static String DidLogout = "DidLogout";
    public static String WillLogout = "WillLogout";

    public static String User = "User";
    public static String Thread = "Thread";
    public static String Message = "Message";

    public static String UserDidConnect = "UserDidConnect";
    public static String UserWillDisconnect = "UserWillDisconnect";

    public static String ContactWillBeAdded = "ContactWillBeAdded";
    public static String ContactWasAdded = "ContactWasAdded";
    public static String ContactWillBeDeleted = "ContactWillBeDeleted";
    public static String ContactWasDeleted = "ContactWasDeleted";

    public static HashMap<String, Object> userData (co.chatsdk.core.dao.User user) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(HookEvent.User, user);
        return data;
    }

    public static HashMap<String, Object> messageData (co.chatsdk.core.dao.Message message) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(HookEvent.Message, message);
        return data;
    }

    public static HashMap<String, Object> threadData (co.chatsdk.core.dao.Thread thread) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(HookEvent.Thread, thread);
        return data;
    }

}
