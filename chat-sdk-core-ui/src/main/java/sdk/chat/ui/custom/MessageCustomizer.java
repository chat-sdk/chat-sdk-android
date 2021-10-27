package sdk.chat.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;

public class MessageCustomizer implements MessageHolders.ContentChecker<MessageHolder> {

    /**
     * @deprecated use {@link ChatSDKUI#shared().getMessageCustomizer() }
     */
    @Deprecated
    public static MessageCustomizer shared() {
        return ChatSDKUI.shared().getMessageCustomizer();
    }

    Map<Byte, IMessageHandler> messageHandlers = new HashMap<>();
    {
        addMessageHandler(new TextMessageHandler());
        addMessageHandler(new ImageMessageHandler());
        addMessageHandler(new SystemMessageHandler());
        addMessageHandler(new Base64ImageMessageHandler());
    }

    public Collection<IMessageHandler> getMessageHandlers() {
        return messageHandlers.values();
    }

    public void onBindMessageHolders(Context context, MessageHolders holders) {
        for (IMessageHandler handler: getMessageHandlers()) {
            handler.onBindMessageHolders(context, holders);
        }
    }

    public void addMessageHandler(IMessageHandler handler) {
        for (Byte type: handler.getTypes()) {
            messageHandlers.put(type, handler);
        }
    }

    /**
     * Return message holder, the last non-null holder registered will be returned
     */
    public MessageHolder onNewMessageHolder(Message message) {
        IMessageHandler handler = handlerForMessage(message);
        if (handler != null) {
            return handler.onNewMessageHolder(message);
        }
        return null;
    }

    public void onClick(Activity activity, View rootView, Message message) {
        IMessageHandler handler = handlerForMessage(message);
        if (handler != null) {
            handler.onClick(activity, rootView, message);
        }
    }


    public IMessageHandler handlerForMessage(Message message) {
        return messageHandlers.get(message.getType().byteValue());
    }

    public void onLongClick(Activity activity, View rootView, Message message) {
        IMessageHandler handler = handlerForMessage(message);
        if (handler != null) {
            handler.onLongClick(activity, rootView, message);
        }
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        IMessageHandler handler = messageHandlers.get(type);
        if (handler != null) {
            return handler.hasContentFor(message);
        }
        return false;
    }

    public void stop() {
        messageHandlers.clear();
    }

}
