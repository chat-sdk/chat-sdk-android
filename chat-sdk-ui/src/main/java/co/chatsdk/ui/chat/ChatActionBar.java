package co.chatsdk.ui.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.appbar.AppBarLayout;
import co.chatsdk.core.dao.Thread;

import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.databinding.ActionBarChatActivityBinding;
import co.chatsdk.ui.utils.ThreadImageBuilder;

public class ChatActionBar extends AppBarLayout {

    protected View.OnClickListener onClickListener;

    protected ActionBarChatActivityBinding b;

    public ChatActionBar(Context context) {
        super(context);
        initViews();
    }

    public ChatActionBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initViews();
    }

    public ChatActionBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    public void initViews() {
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.action_bar_chat_activity, this, true);
        b.titleTextView.setOnClickListener(this::onClick);
        b.imageView.setOnClickListener(this::onClick);
        b.subtitleTextView.setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
        if (ChatSDK.config().threadDetailsEnabled && onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public void reload(Thread thread) {
        String displayName = Strings.nameForThread(thread);
//        setTitle(displayName);
        b.titleTextView.setText(displayName);
        ThreadImageBuilder.load(b.imageView, thread);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setSubtitleText(Thread thread, String text) {
        if(StringChecker.isNullOrEmpty(text)) {
            if(thread.typeIs(ThreadType.Private1to1)) {
                text = getContext().getString(R.string.tap_here_for_contact_info);
            } else {
                text = thread.getUserListString();
            }
        }
        b.subtitleTextView.setText(text);
    }

    public void hideText() {
        b.titleTextView.setVisibility(View.GONE);
        b.subtitleTextView.setVisibility(View.GONE);
    }

    public void showText() {
        b.titleTextView.setVisibility(View.VISIBLE);
        b.subtitleTextView.setVisibility(View.VISIBLE);
    }

    public Toolbar getToolbar() {
        return b.toolbar;
    }
}
