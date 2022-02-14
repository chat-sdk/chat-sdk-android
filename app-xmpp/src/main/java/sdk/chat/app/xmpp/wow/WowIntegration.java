package sdk.chat.app.xmpp.wow;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.demo.xmpp.R;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.module.UIModule;

public class WowIntegration {

    public static void integrate() {

        ChatSDK.ui().setChatActivity(ChatActivity.class);
        ChatSDKUI.setChatFragmentProvider(WowChatFragment::new);


        //        ChatSDK.ui().setChatActivity(WowChatActivity.class);
        ChatSDKUI.shared().getMessageCustomizer().addMessageHandler(new WowTextMessageHandler());
        ChatSDKUI.shared().getMessageCustomizer().addMessageHandler(new WowImageMessageHandler());
        UIModule.config().setTheme(R.style.AppTheme);
        UIModule.config().showAvatarInChatView = false;
        UIModule.shared().setOnlineStatusBinder(new WowOnlineStatusBinder());



    }

}
