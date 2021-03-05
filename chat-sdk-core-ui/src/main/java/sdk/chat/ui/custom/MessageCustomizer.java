package sdk.chat.ui.custom;

import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.model.MessageHolder;

public class MessageCustomizer implements MessageHolders.ContentChecker<MessageHolder> {

    /**
     * @deprecated use {@link ChatSDKUI#shared().getMessageCustomizer() }
     */
    @Deprecated
    public static MessageCustomizer shared() {
        return ChatSDKUI.shared().getMessageCustomizer();
    }

//    protected List<IMessageHandler> messageHandlers = new ArrayList<>();

    protected IMessageHandler textMessageHandler = new TextMessageHandler();
    protected IMessageHandler imageMessageHandler = new ImageMessageHandler();
    protected IMessageHandler systemMessageHandler = new SystemMessageHandler();

    Map<Byte, IMessageHandler> messageHandlers = new HashMap<>();
    {
        addMessageHandler(textMessageHandler);
        addMessageHandler(imageMessageHandler);
        addMessageHandler(systemMessageHandler);
    }

    public void setTextMessageHandler(TextMessageHandler handler) {
        this.textMessageHandler = handler;
    }

    public void setTextMessageHandler(ImageMessageHandler handler) {
        this.imageMessageHandler = handler;
    }

    public void setTextMessageHandler(SystemMessageHandler handler) {
        this.systemMessageHandler = handler;
    }

    public Collection<IMessageHandler> getMessageHandlers() {
//        List<IMessageHandler> handlers = new ArrayList<>();
//        handlers.add(textMessageHandler);
//        handlers.add(imageMessageHandler);
//        handlers.add(systemMessageHandler);
//        handlers.addAll(messageHandlers);
//        return handlers;
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
        MessageHolder holder;
        for (IMessageHandler handler: getMessageHandlers()) {
            holder = handler.onNewMessageHolder(message);
            if (holder != null) {
                return holder;
            }
        }
        return null;
    }

    public void onClick(ChatActivity activity, View rootView, Message message) {
        for (IMessageHandler handler: getMessageHandlers()) {
            handler.onClick(activity, rootView, message);
        }
    }

    public void onLongClick(ChatActivity activity, View rootView, Message message) {
        for (IMessageHandler handler: getMessageHandlers()) {
            handler.onLongClick(activity, rootView, message);
        }
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        IMessageHandler handler = messageHandlers.get(type);
        if (handler != null) {
            return handler.hasContentFor(message);
        }
//
//        for (IMessageHandler handler: getMessageHandlers()) {
//            if (handler.hasContentFor(message, type)) {
//                return true;
//            }
//        }
        return false;
    }

    public void stop() {
        messageHandlers.clear();
    }

}
