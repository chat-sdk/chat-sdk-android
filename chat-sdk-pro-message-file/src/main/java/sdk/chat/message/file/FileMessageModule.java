package sdk.chat.message.file;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.MessageHandler;
import sdk.chat.core.module.AbstractModule;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import sdk.chat.licensing.Report;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;
import sdk.chat.ui.custom.CustomMessageHandler;

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
        Report.shared().add(getName());


        ChatSDKUI.shared().getMessageCustomizer().addMessageHandler(new CustomMessageHandler() {

            @Override
            public List<Byte> getTypes() {
                return types(MessageType.File);
            }

            @Override
            public boolean hasContentFor(MessageHolder holder) {
                return holder.getClass().equals(FileMessageHolder.class);
            }

            @Override
            public void onBindMessageHolders(Context ctx, MessageHolders holders) {
                holders.registerContentType(
                        (byte) MessageType.File,
                        IncomingFileMessageViewHolder.class,
                        R.layout.view_holder_incoming_text_message,
                        OutcomingFileMessageViewHolder.class,
                        R.layout.view_holder_outcoming_text_message,
                        ChatSDKUI.shared().getMessageCustomizer());
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.File)) {
                    return new FileMessageHolder(message);
                }
                return null;
            }

            @Override
            public boolean onClick(Activity activity, View rootView, Message message) {
                if (!super.onClick(activity, rootView, message)) {
                    if (message.getMessageType().is(MessageType.File)) {
                        String url = message.stringForKey(Keys.MessageFileURL);
                        Uri uri = Uri.parse(url);
                        final String mimeType = message.stringForKey(Keys.MessageMimeType);

                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(uri.toString()), mimeType);
                            activity.startActivity(intent);
                            return true;
                        } catch (ActivityNotFoundException e) {
                            Logger.debug(e);
                            activity.startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW, uri), activity.getText(R.string.open_with)));
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onLongClick(Activity activity, View rootView, Message message) {
                return false;
            }
        });
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
