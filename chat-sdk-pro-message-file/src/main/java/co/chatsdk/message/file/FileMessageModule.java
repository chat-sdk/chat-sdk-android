package co.chatsdk.message.file;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import org.pmw.tinylog.Logger;

import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.handlers.Module;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;
import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.custom.Customiser;
import co.chatsdk.ui.custom.IMessageHandler;

/**
 * Created by Pepe on 01/05/18.
 */

public class FileMessageModule implements Module {

    public static final int CHOOSE_FILE = 42;

    public static final FileMessageModule instance = new FileMessageModule();

    public static FileMessageModule shared() {
        return instance;
    }

    @Override
    public void activate(Context context) {
        ChatSDK.a().fileMessage = new BaseFileMessageHandler();
        ChatSDK.ui().addChatOption(new FileChatOption(ChatSDK.shared().context().getString(R.string.file_message)));

        Customiser.shared().addMessageHandler(new IMessageHandler() {
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
                        this);
            }

            @Override
            public MessageHolder onNewMessageHolder(Message message) {
                if (message.getMessageType().is(MessageType.File)) {
                    return new FileMessageHolder(message);
                }
                return null;
            }

            @Override
            public void onClick(Activity activity, View rootView, Message message) {
                if (message.getMessageType().is(MessageType.File)) {
                    String url = (String) message.valueForKey(Keys.MessageFileURL);
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
            public void onLongClick(Activity activity, View rootView, Message message) {

            }
        });
    }

    @Override
    public String getName() {
        return "FileMessageModule";
    }
}
