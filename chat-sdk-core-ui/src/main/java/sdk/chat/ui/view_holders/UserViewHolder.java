package sdk.chat.ui.view_holders;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
import sdk.chat.ui.binders.OnlineStatusBinder;
import sdk.chat.ui.module.UIModule;

public class UserViewHolder extends RecyclerView.ViewHolder  {

    protected boolean multiSelectEnabled;

    @BindView(R2.id.avatarImageView) protected CircleImageView avatarImageView;
    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.nameTextView) protected TextView nameTextView;
    @BindView(R2.id.checkbox) protected CheckBox checkbox;
    @BindView(R2.id.statusTextView) protected TextView statusTextView;
    @BindView(R2.id.availabilityImageView) protected ImageView availabilityImageView;
    @BindView(R2.id.root) protected RelativeLayout root;

    UsersListAdapter.SubtitleProvider provider;

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

        nameTextView.setText(item.getName());

        if (multiSelectEnabled) {
            checkbox.setVisibility(View.VISIBLE);
        } else {
            checkbox.setVisibility(View.INVISIBLE);
        }

        if (StringChecker.isNullOrEmpty(item.getAvailability()) || multiSelectEnabled) {
            availabilityImageView.setVisibility(View.INVISIBLE);
        } else {
            availabilityImageView.setVisibility(View.VISIBLE);
            availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(item.getAvailability()));
        }

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
                    .placeholder(UIModule.config().defaultProfilePlaceholder)
                    .override(width, height)
                    .into(avatarImageView);
        } else {
            avatarImageView.setImageResource(UIModule.config().defaultProfilePlaceholder);
        }
    }

    public CheckBox getCheckbox() {
        return checkbox;
    }
}
