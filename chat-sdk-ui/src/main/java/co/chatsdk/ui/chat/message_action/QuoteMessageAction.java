package co.chatsdk.ui.chat.message_action;

import android.app.Activity;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
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

    @Override
    public Completable execute(Activity activity) {
        return Completable.create(emitter -> {
            if(activity instanceof ChatActivity) {
                ChatActivity ca = (ChatActivity) activity;
                Message quotedMessage = message.get();
                //String imageURL = quotedMessage I must get the URL here.
                ca.displayQuoteView(quotedMessage);
                //Need to make one more here for the image
                // Make a method on the chat activity to show and hide the reply view over the textInputView
            }
        });
    }
}
