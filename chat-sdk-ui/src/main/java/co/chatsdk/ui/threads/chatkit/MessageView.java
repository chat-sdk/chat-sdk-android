package co.chatsdk.ui.threads.chatkit;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

import co.chatsdk.core.dao.Message;

public class MessageView implements IMessage {

    public Message message;

    public MessageView(Message message) {
        this.message = message;
    }

    @Override
    public String getId() {
        return message.getEntityID();
    }

    @Override
    public String getText() {
        return message.getText();
    }

    @Override
    public IUser getUser() {
        return new UserView(message.getSender());
    }

    @Override
    public Date getCreatedAt() {
        return message.getDate().toDate();
    }
}
