package co.chatsdk.ui;

import co.chatsdk.core.session.ChatSDK;

public class ChatSDKUI {

    public static BaseInterfaceAdapter api() {
        if (ChatSDK.ui() instanceof BaseInterfaceAdapter) {
            return (BaseInterfaceAdapter) ChatSDK.ui();
        }
        return null;
    }

}
