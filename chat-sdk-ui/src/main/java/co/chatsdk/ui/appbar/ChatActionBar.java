package co.chatsdk.ui.appbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.AppBarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.utils.ThreadImageBuilder;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActionBar extends AppBarLayout {

    protected OnClickListener onClickListener;
    @BindView(R2.id.titleTextView) protected TextView titleTextView;
    @BindView(R2.id.imageView) protected CircleImageView imageView;
    @BindView(R2.id.subtitleTextView) protected TextView subtitleTextView;
    @BindView(R2.id.searchImageView) protected ImageView searchImageView;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.appBarLayout) protected AppBarLayout appBarLayout;

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
        LayoutInflater.from(getContext()).inflate(R.layout.app_bar_chat, this);
        ButterKnife.bind(this);

        titleTextView.setOnClickListener(this::onClick);
        imageView.setOnClickListener(this::onClick);
        subtitleTextView.setOnClickListener(this::onClick);
        searchImageView.setImageDrawable(Icons.get(Icons.choose().search, R.color.app_bar_icon_color));
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
        ThreadImageBuilder.load(imageView, thread);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setSubtitleText(Thread thread, String text) {
        if (StringChecker.isNullOrEmpty(text)) {
            if (thread.typeIs(ThreadType.Private1to1)) {
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

    public void onSearchClicked(OnClickListener listener) {
        searchImageView.setOnClickListener(listener);
    }

    public void hideSearchIcon() {
        searchImageView.setVisibility(View.INVISIBLE);
    }

    public void showSearchIcon() {
        searchImageView.setVisibility(View.VISIBLE);
    }
}
