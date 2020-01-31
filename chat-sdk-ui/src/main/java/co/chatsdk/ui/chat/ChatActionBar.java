package co.chatsdk.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import co.chatsdk.core.dao.Thread;

import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.image.ThreadImageBuilder;

public class ChatActionBar {

    protected TextView titleTextView;
    protected TextView subtitleTextView;
    protected View actionBarView;
    protected ImageView threadImageView;
    protected View.OnClickListener onClickListener;

    public ChatActionBar(LayoutInflater inflater) {

        actionBarView = inflater.inflate(R.layout.action_bar_chat_activity, null);

        titleTextView = actionBarView.findViewById(R.id.text_name);
        subtitleTextView = actionBarView.findViewById(R.id.text_subtitle);
        threadImageView = actionBarView.findViewById(R.id.image_avatar);

        titleTextView.setOnClickListener(this::onClick);
        threadImageView.setOnClickListener(this::onClick);
        subtitleTextView.setOnClickListener(this::onClick);

    }

    public void onClick(View view) {
        if (ChatSDK.config().threadDetailsEnabled && onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public View get() {
        return actionBarView;
    }

    public void reload(Thread thread) {
        String displayName = Strings.nameForThread(thread);
//        setTitle(displayName);
        titleTextView.setText(displayName);
        ThreadImageBuilder.load(threadImageView, thread);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setSubtitleText(Thread thread, String text) {
        if(StringChecker.isNullOrEmpty(text)) {
            if(thread.typeIs(ThreadType.Private1to1)) {
                text = actionBarView.getContext().getString(R.string.tap_here_for_contact_info);
            } else {
                text = thread.getUserListString();
            }
        }
        subtitleTextView.setText(text);
    }

    public void hideText() {
        titleTextView.setVisibility(View.GONE);
        subtitleTextView.setVisibility(View.GONE);
    }

    public void showText() {
        titleTextView.setVisibility(View.VISIBLE);
        subtitleTextView.setVisibility(View.VISIBLE);
    }
}
