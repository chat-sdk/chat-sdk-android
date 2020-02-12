package co.chatsdk.ui.chat;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.messages.MessagesList;

public class ChatView extends MessagesList {

    public ChatView(Context context) {
        super(context);
        initViews();
    }

    public ChatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ChatView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    public void initViews() {

    }

}
