package sdk.chat.message.file;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import org.pmw.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.MessageCustomizer;
import sdk.chat.ui.custom.IMessageHandler;

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
        ChatSDK.ui().addChatOption(new FileChatOption(ChatSDK.shared().context().getString(R.string.file_message)));

        MessageCustomizer.shared().addMessageHandler(new IMessageHandler() {
            @Override
            public boolean hasContentFor(MessageHolder message, byte type) {
                return type == MessageType.File && message instanceof FileMessageHolder;
            }

            @Override
            public void onBindMessageHolders(Context ctx, MessageHolders holders) {
                holders.registerContentType(
                        (byte) MessageType.File,
                        IncomingFileMessageViewHolder.class,
                        R.layout.view_holder_incoming_text_message,
                        OutcomingFileMessageViewHolder.class,
                        R.layout.view_holder_outcoming_text_message,
                        MessageCustomizer.shared());
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.File)) {
                    return new FileMessageHolder(message);
                }
                return null;
            }

            @Override
            public void onClick(ChatActivity activity, View rootView, Message message) {
                if (message.getMessageType().is(MessageType.File)) {
                    String url = message.stringForKey(Keys.MessageFileURL);
                    Uri uri = Uri.parse(url);
                    final String mimeType = message.stringForKey(Keys.MessageMimeType);

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(uri.toString()), mimeType);
                        activity.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Logger.debug(e);
                        activity.startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, uri), activity.getText(R.string.open_with)));
                    }
                }
            }

            @Override
            public void onLongClick(ChatActivity activity, View rootView, Message message) {

            }
        });
    }

    @Override
    public String getName() {
        return "FileMessageModule";
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
