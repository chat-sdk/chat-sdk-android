package co.chatsdk.ui.chat.message_action;

import android.app.Activity;
import android.widget.Toast;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;
import co.chatsdk.ui.chat.ChatActivity;
import io.reactivex.Completable;

public class QuoteMessageAction extends MessageAction {

    protected int messageQuoteActivityCode = 1004;

    protected DisposableList disposableList = new DisposableList();

    public QuoteMessageAction(Message message) {
        super(message);
        type = Type.Quote;
        titleResourceId = R.string.quote;
        iconResourceId = R.drawable.ic_format_quote_white_24dp;
        colorId = R.color.button_success;
        successMessageId = R.string.quote_message_created;
    }
//This will ONLY work if the message to be quoted is a text message, otherwise the quote cannot be made, see line 40.
    @Override
    public Completable execute(Activity activity) {
        return Completable.create(emitter -> {
            if(activity instanceof ChatActivity) {
                ChatActivity chatActivity = (ChatActivity) activity;
                Message quotedMessage = message.get();
                if (quotedMessage.getType() == MessageType.Text) {
                    chatActivity.setQuoteBoolean(true);
                    chatActivity.displayQuoteView(quotedMessage);
                }
                else {
                    chatActivity.sayOnlyTextMessageCanBeQuoted();
                }
            }
        });
    }
}
