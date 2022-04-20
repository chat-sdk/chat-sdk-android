package sdk.chat.message.file;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.licensing.Report;
import sdk.chat.ui.ChatSDKUI;

/**
 * Created by Pepe on 01/05/18.
 */

public class FileMessageModule extends AbstractModule {

    public static final int CHOOSE_FILE = 42;

    public static final FileMessageModule instance = new FileMessageModule();

    public static FileMessageModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().fileMessage = new BaseFileMessageHandler();

        ChatSDK.ui().addChatOption(new FileChatOption(R.string.file_message, R.drawable.icn_100_file));
        Report.shared().add(getName());

        ChatSDKUI.shared().getMessageRegistrationManager().addMessageRegistration(new FileMessageRegistration());
    }

    @Override
    public MessageHandler getMessageHandler() {
        return ChatSDK.fileMessage();
    }

    @Override
    public List<String> requiredPermissions() {
        List<String> permissions = new ArrayList<>();

//        permissions.add(Manifest.permission.MANAGE_DOCUMENTS);

        return permissions;
    }

    @Override
    public void stop() {
    }

}
