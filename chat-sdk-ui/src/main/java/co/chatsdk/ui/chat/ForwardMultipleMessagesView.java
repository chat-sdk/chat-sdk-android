package co.chatsdk.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import co.chatsdk.ui.R;

public class ForwardMultipleMessagesView extends LinearLayout {

    protected ImageButton buttonSend;
    protected ImageButton buttonBack;
    protected TextView messagesSelected;

    public ForwardMultipleMessagesView(Context context) {
        super(context);
        init();
    }

    public ForwardMultipleMessagesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForwardMultipleMessagesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init() {
        inflate(getContext(), R.layout.view_chat_forward_multiple_messages, this);
    }

    protected void initViews() {
        buttonSend = findViewById(R.id.button_back);
        buttonBack = findViewById(R.id.button_send);
        messagesSelected = findViewById(R.id.text_messages_selected);
    }

    protected Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();

        if (isInEditMode()) {
            return;
        }
    }
}
