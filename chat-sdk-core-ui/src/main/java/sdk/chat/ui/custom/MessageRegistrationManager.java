package sdk.chat.ui.custom;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import androidx.annotation.LayoutRes;

import com.stfalcon.chatkit.commons.ViewHolder;
import com.stfalcon.chatkit.messages.MessageHolders;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import sdk.chat.core.dao.Message;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.chat.model.MessageHolder;

public class MessageRegistrationManager implements MessageHolders.ContentChecker<MessageHolder> {

    /**
     * @deprecated use {@link ChatSDKUI#shared().getMessageCustomizer() }
     */
    @Deprecated
    public static MessageRegistrationManager shared() {
        return ChatSDKUI.shared().getMessageRegistrationManager();
    }

    protected Map<Byte, MessageRegistration> messageRegistrations = new HashMap<>();
    {
        addMessageRegistration(new TextMessageRegistration());
        addMessageRegistration(new ImageMessageRegistration());
        addMessageRegistration(new SystemMessageRegistration());
        addMessageRegistration(new Base64ImageMessageRegistration());
    }

    protected Class<? extends ViewHolder<Date>> dateHolder;
    protected @LayoutRes int dateLayout;

    public Collection<MessageRegistration> getMessageRegistrations() {
        return messageRegistrations.values();
    }

    public void onBindMessageHolders(Context context, MessageHolders holders) {
        for (MessageRegistration handler: getMessageRegistrations()) {
            handler.onBindMessageHolders(context, holders);
        }
        if (dateHolder != null) {
            holders.setDateHeaderHolder(dateHolder);
        }
        if (dateLayout != 0) {
            holders.setDateHeaderLayout(dateLayout);
        }
    }

    public void addMessageRegistration(MessageRegistration registration) {
        for (Byte type: registration.getTypes()) {
            messageRegistrations.put(type, registration);
        }
    }

    /**
     * Return message holder, the last non-null holder registered will be returned
     */
    public MessageHolder onNewMessageHolder(Message message) {
        MessageRegistration handler = handlerForMessage(message);
        if (handler != null) {
            return handler.onNewMessageHolder(message);
        }
        return null;
    }

    public boolean onClick(Activity activity, View rootView, Message message) {
        MessageRegistration handler = handlerForMessage(message);
        if (handler != null) {
            return handler.onClick(activity, rootView, message);
        }
        return false;
    }

    public MessageRegistration handlerForMessage(Message message) {
        return messageRegistrations.get(message.getType().byteValue());
    }

    public void onLongClick(Activity activity, View rootView, Message message) {
        MessageRegistration handler = handlerForMessage(message);
        if (handler != null) {
            handler.onLongClick(activity, rootView, message);
        }
    }

    @Override
    public boolean hasContentFor(MessageHolder message, byte type) {
        MessageRegistration handler = messageRegistrations.get(type);
        if (handler != null) {
            return handler.hasContentFor(message);
        }
        return false;
    }

    public void stop() {
        messageRegistrations.clear();
    }

    public void setDateHolder(Class<? extends ViewHolder<Date>> dateHolder) {
        this.dateHolder = dateHolder;
    }

    public void setDateLayout(int dateLayout) {
        this.dateLayout = dateLayout;
    }


}
