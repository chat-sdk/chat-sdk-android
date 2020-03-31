package co.chatsdk.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.ui.chat.model.MessageHolder;

public class Customiser implements IMessageHandler {

    static final Customiser instance = new Customiser();

    public static Customiser shared() {
        return instance;
    }

    protected List<IMessageHandler> messageHandlers = new ArrayList<>();

    public Customiser() {
        messageHandlers.add(new MessageHandler());
    }

    public List<IMessageHandler> getMessageHandlers() {
        return messageHandlers;
    }

    @Override
    public void onBindMessageHolders(Context context, MessageHolders holders) {
        for (IMessageHandler handler: messageHandlers) {
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
        for (IMessageHandler handler: messageHandlers) {
            holder = handler.onNewMessageHolder(message);
            if (holder != null) {
                return holder;
            }
        }
        return null;
    }

    @Override
    public void onClick(Activity activity, View rootView, Message message) {
        for (IMessageHandler handler: messageHandlers) {
            handler.onClick(activity, rootView, message);
        }
    }

    @Override
    public void onLongClick(Activity activity, View rootView, Message message) {
        for (IMessageHandler handler: messageHandlers) {
            handler.onLongClick(activity, rootView, message);
        }
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        for (IMessageHandler handler: messageHandlers) {
            if (handler.hasContentFor(message, type)) {
                return true;
            }
        }
        return false;
    }
}
