package co.chatsdk.ui.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;
import com.mikepenz.iconics.Iconics;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.Thread;

import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.utils.ThreadImageBuilder;

public class ChatActionBar extends AppBarLayout {

    @BindView(R2.id.text_name) protected TextView titleTextView;
    @BindView(R2.id.text_subtitle) protected TextView subtitleTextView;
    @BindView(R2.id.image_avatar) protected ImageView threadImageView;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;

    protected View.OnClickListener onClickListener;

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
        inflate(getContext(), R.layout.action_bar_chat_activity, this);

        ButterKnife.bind(this);

        titleTextView.setOnClickListener(this::onClick);
        threadImageView.setOnClickListener(this::onClick);
        subtitleTextView.setOnClickListener(this::onClick);
    }

    public void onClick(View view) {
        if (ChatSDK.config().threadDetailsEnabled && onClickListener != null) {
            onClickListener.onClick(view);
        }
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
                text = getContext().getString(R.string.tap_here_for_contact_info);
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

    public Toolbar getToolbar() {
        return toolbar;
    }
}
