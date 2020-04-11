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

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.core.utils.Strings;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.icons.Icons;
import co.chatsdk.ui.module.DefaultUIModule;
import co.chatsdk.ui.utils.ThreadImageBuilder;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class ChatActionBar extends AppBarLayout {

    protected OnClickListener onClickListener;
    @BindView(R2.id.titleTextView) protected TextView titleTextView;
    @BindView(R2.id.imageView) protected CircleImageView imageView;
    @BindView(R2.id.subtitleTextView) protected TextView subtitleTextView;
    @BindView(R2.id.searchImageView) protected ImageView searchImageView;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.appBarLayout) protected AppBarLayout appBarLayout;

    protected Disposable lastOnlineDisposable;

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
        searchImageView.setImageDrawable(Icons.get(Icons.choose().search, Icons.shared().actionBarIconColor));
    }

    public void onClick(View view) {
        if (DefaultUIModule.config().threadDetailsEnabled && onClickListener != null) {
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
                if (thread.otherUser() != null) {
                    if (ChatSDK.lastOnline() != null) {
                        if (lastOnlineDisposable != null) {
                            lastOnlineDisposable.dispose();
                        }
                        lastOnlineDisposable = ChatSDK.lastOnline().getLastOnline(thread.otherUser())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe((date, throwable) -> {
                                    if (throwable == null && date != null) {
                                        Locale current = getResources().getConfiguration().locale;
                                        PrettyTime pt = new PrettyTime(current);
                                        if (thread.otherUser().getIsOnline()) {
                                            subtitleTextView.setText(getContext().getString(R.string.online));
                                        } else {
                                            subtitleTextView.setText(String.format(getContext().getString(R.string.last_seen__), pt.format(date)));
                                        }
                                    }
                                });
                    } else {
                        if (thread.otherUser().getIsOnline()) {
                            text = getContext().getString(R.string.online);
                        }
                    }
                }
                if (StringChecker.isNullOrEmpty(text)) {
                    text = getContext().getString(R.string.tap_here_for_contact_info);
                }
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
