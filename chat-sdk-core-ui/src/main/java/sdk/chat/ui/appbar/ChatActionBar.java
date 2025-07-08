package sdk.chat.ui.appbar;

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

import java.util.concurrent.Callable;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.core.utils.Strings;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.RX;

public class ChatActionBar extends AppBarLayout {

    protected OnClickListener onClickListener;
    public TextView titleTextView;
    public CircleImageView imageView;
    public TextView subtitleTextView;
    public ImageView searchImageView;
    public Toolbar toolbar;
    public AppBarLayout appBarLayout;

    final PrettyTime pt = new PrettyTime(CurrentLocale.get());

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

        titleTextView = findViewById(R.id.titleTextView);
        imageView = findViewById(R.id.imageView);
        subtitleTextView = findViewById(R.id.subtitleTextView);
        searchImageView = findViewById(R.id.searchImageView);
        toolbar = findViewById(R.id.toolbar);
        appBarLayout = findViewById(R.id.appBarLayout);


        titleTextView.setOnClickListener(this::onClick);
        imageView.setOnClickListener(this::onClick);
        subtitleTextView.setOnClickListener(this::onClick);
        searchImageView.setImageDrawable(ChatSDKUI.icons().get(getContext(), ChatSDKUI.icons().search, ChatSDKUI.icons().actionBarIconColor));
    }

    public void onClick(View view) {
        if (UIModule.config().threadDetailsEnabled && onClickListener != null) {
            onClickListener.onClick(view);
        }
    }

    public void reload(ThreadX thread) {
        String displayName = Strings.nameForThread(thread);
//        setTitle(displayName);
        titleTextView.setText(displayName);

        ChatSDKUI.provider().imageLoader().loadThread(imageView, thread, R.dimen.action_bar_avatar_size);

        setSubtitleText(thread, null);

    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setTypingText(ThreadX thread, final String text) {
        setSubtitleText(thread, text);
    }

    public void setSubtitleText(ThreadX thread, final String text) {
        if (StringChecker.isNullOrEmpty(text)) {

            final String defaultText = getDefaultText();

            ChatSDK.events().disposeOnLogout(Single.defer((Callable<SingleSource<String>>) () -> {
                if (thread.typeIs(ThreadType.Private1to1)) {
                    if (thread.otherUser() != null) {
                        if (ChatSDK.lastOnline() != null) {
                            return ChatSDK.lastOnline().getLastOnline(thread.otherUser()).map(date -> {
                                if (!date.isEmpty()) {
                                    if (thread.otherUser().getIsOnline()) {
                                        return getContext().getString(R.string.online);
                                    } else {
                                        return String.format(getContext().getString(R.string.last_seen__), pt.format(date.get()));
                                    }
                                }
                                return defaultText;
                            });
                        } else {
                            if (thread.otherUser().getIsOnline()) {
                                return Single.just(getContext().getString(R.string.online));
                            }
                        }
                    }
                    return Single.just(defaultText);
                } else {
                    return Single.just(thread.getUserListString());
                }
            }).subscribeOn(RX.computation()).observeOn(RX.main()).subscribe(output -> {
                subtitleTextView.setText(output);
            }, ChatSDK.events()));
        } else {
            subtitleTextView.setText(text);
        }
    }

    public String getDefaultText() {
        return getContext().getString(R.string.tap_here_for_contact_info);
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
        if (UIModule.config().messageSearchEnabled) {
            searchImageView.setVisibility(View.VISIBLE);
        }
    }
}
