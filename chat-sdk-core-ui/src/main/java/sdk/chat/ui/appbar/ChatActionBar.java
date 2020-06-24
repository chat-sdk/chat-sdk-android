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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.CurrentLocale;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.core.utils.Strings;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.module.UIModule;
import sdk.chat.ui.utils.ThreadImageBuilder;
import sdk.guru.common.RX;

public class ChatActionBar extends AppBarLayout {

    protected OnClickListener onClickListener;
    @BindView(R2.id.titleTextView) protected TextView titleTextView;
    @BindView(R2.id.imageView) protected CircleImageView imageView;
    @BindView(R2.id.subtitleTextView) protected TextView subtitleTextView;
    @BindView(R2.id.searchImageView) protected ImageView searchImageView;
    @BindView(R2.id.toolbar) protected Toolbar toolbar;
    @BindView(R2.id.appBarLayout) protected AppBarLayout appBarLayout;

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
        ButterKnife.bind(this);

        titleTextView.setOnClickListener(this::onClick);
        imageView.setOnClickListener(this::onClick);
        subtitleTextView.setOnClickListener(this::onClick);
        searchImageView.setImageDrawable(Icons.get(getContext(), Icons.choose().search, Icons.shared().actionBarIconColor));
    }

    public void onClick(View view) {
        if (UIModule.config().threadDetailsEnabled && onClickListener != null) {
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

    public void setSubtitleText(Thread thread, final String text) {
        if (StringChecker.isNullOrEmpty(text)) {

            final String defaultText = getContext().getString(R.string.tap_here_for_contact_info);

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
            }));
        } else {
            subtitleTextView.setText(text);
        }
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
