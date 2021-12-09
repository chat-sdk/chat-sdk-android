package sdk.chat.ui.view_holders;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import sdk.chat.core.interfaces.UserListItem;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.Dimen;
import sdk.chat.core.utils.StringChecker;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.binders.AvailabilityHelper;
import sdk.chat.ui.module.UIModule;
import smartadapter.viewholder.SmartViewHolder;

public class UserViewHolder extends SmartViewHolder<UserListItem> {

    protected boolean multiSelectEnabled = false;

    @BindView(R2.id.avatarImageView) protected CircleImageView avatarImageView;
    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.nameTextView) protected TextView nameTextView;
    @BindView(R2.id.checkbox) protected CheckBox checkbox;
    @BindView(R2.id.statusTextView) protected TextView statusTextView;
    @BindView(R2.id.availabilityImageView) protected ImageView availabilityImageView;
    @BindView(R2.id.root) protected RelativeLayout root;

    UsersListAdapter.SubtitleProvider provider;

    public UserViewHolder(ViewGroup parentView) {
        super(parentView, R.layout.view_user_row);
    }

    public UserViewHolder(View view, boolean multiSelectEnabled) {
        this(view, multiSelectEnabled, null);
    }

    public UserViewHolder(View view, boolean multiSelectEnabled, UsersListAdapter.SubtitleProvider provider) {
        super(view);
        ButterKnife.bind(this, view);
        this.provider = provider;

        this.multiSelectEnabled = multiSelectEnabled;
    }

    public void bind(UserListItem item) {

        setName(item.getName());

        showCheckbox(multiSelectEnabled);

        setAvailability(multiSelectEnabled ? null : item.getAvailability());

        UIModule.shared().getOnlineStatusBinder().bind(onlineIndicator, item.getIsOnline());

        if (provider != null) {
            statusTextView.setText(provider.subtitle(item));
        }  else {
            statusTextView.setText(item.getStatus());
        }

        Context context = ChatSDK.ctx();

        int width = Dimen.from(context, R.dimen.small_avatar_width);
        int height = Dimen.from(context, R.dimen.small_avatar_height);

        String avatarURL = item.getAvatarURL();
        if (!StringChecker.isNullOrEmpty(avatarURL)) {
            Glide.with(root)
                    .load(item.getAvatarURL())
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(UIModule.config().defaultProfilePlaceholder)
                    .override(width, height)
                    .into(avatarImageView);
        } else {
            avatarImageView.setImageResource(UIModule.config().defaultProfilePlaceholder);
        }
    }

    public void setName(String name) {
        nameTextView.setText(name);
    }

    public void showCheckbox(boolean show) {
        checkbox.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    public void setAvailability(@Nullable String availability) {
        if (availability == null) {
            availabilityImageView.setVisibility(View.INVISIBLE);
        } else {
            availabilityImageView.setVisibility(View.VISIBLE);
            availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(availability));
        }
    }

    public CheckBox getCheckbox() {
        return checkbox;
    }
}
