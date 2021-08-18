package app.xmpp.adapter.utils;

import sdk.chat.core.session.ChatSDK;

public class ActionMessageCache {

    public static String ignore = "ignore-message-";

    public ActionMessageCache() {
    }

    public static boolean addMessageToIgnore(String messageId) {
        return ChatSDK.shared().getPreferences().edit().putBoolean(ignore + messageId, true).commit();
    }

    public static boolean shouldIgnore(String messageId) {
        return ChatSDK.shared().getPreferences().getBoolean(ignore + messageId, false);
    }

}
