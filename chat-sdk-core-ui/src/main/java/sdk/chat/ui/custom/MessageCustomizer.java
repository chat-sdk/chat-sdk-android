package sdk.chat.ui.custom;

import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.activities.ChatActivity;
import sdk.chat.ui.chat.model.MessageHolder;

public class MessageCustomizer implements IMessageHandler {

    static final MessageCustomizer instance = new MessageCustomizer();

    public static MessageCustomizer shared() {
        return instance;
    }

    protected List<IMessageHandler> messageHandlers = new ArrayList<>();

    protected IMessageHandler textMessageHandler = new TextMessageHandler();
    protected IMessageHandler imageMessageHandler = new ImageMessageHandler();
    protected IMessageHandler systemMessageHandler = new SystemMessageHandler();

    public void setTextMessageHandler(TextMessageHandler handler) {
        this.textMessageHandler = handler;
    }

    public void setTextMessageHandler(ImageMessageHandler handler) {
        this.imageMessageHandler = handler;
    }

    public void setTextMessageHandler(SystemMessageHandler handler) {
        this.systemMessageHandler = handler;
    }

    public List<IMessageHandler> getMessageHandlers() {
        List<IMessageHandler> handlers = new ArrayList<>();
        handlers.add(textMessageHandler);
        handlers.add(imageMessageHandler);
        handlers.add(systemMessageHandler);
        handlers.addAll(messageHandlers);
        return handlers;
    }

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        for (IMessageHandler handler: getMessageHandlers()) {
            handler.onBindMessageHolders(context, holders);
        }
    }

    public void addMessageHandler(IMessageHandler handler) {
        messageHandlers.add(0, handler);
    }

    /**
     * Return message holder, the last non-null holder registered will be returned
     */
    @Override
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

    @Override
    public void onClick(ChatActivity activity, View rootView, Message message) {
        for (IMessageHandler handler: getMessageHandlers()) {
            handler.onClick(activity, rootView, message);
        }
    }

    @Override
    public void onLongClick(ChatActivity activity, View rootView, Message message) {
        for (IMessageHandler handler: getMessageHandlers()) {
            handler.onLongClick(activity, rootView, message);
        }
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        for (IMessageHandler handler: getMessageHandlers()) {
            if (handler.hasContentFor(message, type)) {
                return true;
            }
        }
        return false;
    }

    public void stop() {
        messageHandlers.clear();
    }
}
