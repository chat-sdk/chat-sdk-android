package co.chatsdk.core.session;

import co.chatsdk.core.interfaces.InterfaceAdapter;

/**
 * Created by benjaminsmiley-andrews on 12/07/2017.
 */

public class InterfaceManager {

    public static String USER_ENTITY_ID = "chat_sdk_user_entity_id";
    public static final String THREAD_ENTITY_ID = "chat_sdk_thread_entity_id";
    public static final String PUSH_TITLE = "chat_sdk_push_title";
    public static final String PUSH_BODY = "chat_sdk_push_body";

    public static final String ATTEMPT_CACHED_LOGIN = "ATTEMPT_CACHED_LOGIN";

    private final static InterfaceManager instance = new InterfaceManager();
    public InterfaceAdapter a;

    protected InterfaceManager () {

    }
    public static InterfaceManager shared () {
        return instance;
    }


}
