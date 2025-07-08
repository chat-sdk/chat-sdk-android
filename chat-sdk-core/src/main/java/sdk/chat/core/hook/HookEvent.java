package sdk.chat.core.hook;

import java.util.HashMap;

import sdk.chat.core.dao.ThreadX;

public class HookEvent {

    public static String DidAuthenticate = "DidAuthenticate";

    public static String UserOn = "UserOn";

    public static String MessageReceived = "MessageReceived";
    public static String MessageWillSend = "MessageWillSend";
    public static String MessageSent = "MessageSent";
    public static String IsNew_Boolean = "IsNew_Boolean";

    public static String DidLogout = "DidLogout";
    public static String WillLogout = "WillLogout";

    public static String User = "User";
    public static String Thread = "Thread";
    public static String Message = "Message";
    public static String Object = "Object";

    public static String UserDidConnect = "UserDidConnect";
    public static String UserWillDisconnect = "UserWillDisconnect";

    public static String ContactWillBeAdded = "ContactWillBeAdded";
    public static String ContactWasAdded = "ContactWasAdded";
    public static String ContactWillBeDeleted = "ContactWillBeDeleted";
    public static String ContactWasDeleted = "ContactWasDeleted";

    public static String AppWasDismissedFromTray = "AppWasDismissedFromTray";

    public static HashMap<String, Object> userData (sdk.chat.core.dao.User user) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(HookEvent.User, user);
        return data;
    }

    public static HashMap<String, Object> messageData (sdk.chat.core.dao.Message message) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(HookEvent.Message, message);
        return data;
    }

    public static HashMap<String, Object> threadData (ThreadX thread) {
        HashMap<String, Object> data = new HashMap<>();
        data.put(HookEvent.Thread, thread);
        return data;
    }

}
