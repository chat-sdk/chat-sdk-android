package co.chatsdk.ui.appbar;

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
import co.chatsdk.ui.databinding.AppBarChatBinding;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ThreadImageBuilder;

public class ChatActionBar extends AppBarLayout {

    protected View.OnClickListener onClickListener;

    protected AppBarChatBinding b;

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
        b = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.app_bar_chat, this, true);
        b.titleTextView.setOnClickListener(this::onClick);
        b.imageView.setOnClickListener(this::onClick);
        b.subtitleTextView.setOnClickListener(this::onClick);
        b.searchImageView.setImageDrawable(Icons.get(Icons.choose().search, R.color.app_bar_icon_color));
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

    public void onSearchClicked(OnClickListener listener) {
        b.searchImageView.setOnClickListener(listener);
    }

    public void hideSearchIcon() {
        b.searchImageView.setVisibility(View.INVISIBLE);
    }

    public void showSearchIcon() {
        b.searchImageView.setVisibility(View.VISIBLE);
    }
}
