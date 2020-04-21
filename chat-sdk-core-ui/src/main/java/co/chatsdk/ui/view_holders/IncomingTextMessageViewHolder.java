package co.chatsdk.ui.view_holders;

import android.view.View;

import co.chatsdk.ui.chat.model.MessageHolder;
import co.chatsdk.ui.view_holders.base.BaseIncomingTextMessageViewHolder;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Function;


public class IncomingTextMessageViewHolder extends BaseIncomingTextMessageViewHolder<MessageHolder> {
    public IncomingTextMessageViewHolder(View itemView, Object payload) {
        super(itemView, payload);
    }
}
