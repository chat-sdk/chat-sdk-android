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
import sdk.chat.ui.adapters.UsersListAdapter;
import sdk.chat.ui.binders.AvailabilityHelper;
import sdk.chat.ui.module.UIModule;
import smartadapter.viewholder.SmartViewHolder;

public class UserViewHolder extends SmartViewHolder<UserListItem> {

    protected boolean multiSelectEnabled = false;

    protected CircleImageView avatarImageView;
    protected View onlineIndicator;
    protected TextView nameTextView;
    protected CheckBox checkbox;
    protected TextView statusTextView;
    protected ImageView availabilityImageView;
    protected RelativeLayout root;

    UsersListAdapter.SubtitleProvider provider;

    public UserViewHolder(ViewGroup parentView) {

        super(parentView, R.layout.view_user_row);

        avatarImageView = parentView.findViewById(R.id.avatarImageView);
        onlineIndicator = parentView.findViewById(R.id.onlineIndicator);
        nameTextView = parentView.findViewById(R.id.nameTextView);
        checkbox = parentView.findViewById(R.id.checkbox);
        statusTextView = parentView.findViewById(R.id.statusTextView);
        availabilityImageView = parentView.findViewById(R.id.availabilityImageView);
        root = parentView.findViewById(R.id.root);
    }

    public UserViewHolder(View view, boolean multiSelectEnabled) {
        this(view, multiSelectEnabled, null);

        avatarImageView = view.findViewById(R.id.avatarImageView);
        onlineIndicator = view.findViewById(R.id.onlineIndicator);
        nameTextView = view.findViewById(R.id.nameTextView);
        checkbox = view.findViewById(R.id.checkbox);
        statusTextView = view.findViewById(R.id.statusTextView);
        availabilityImageView = view.findViewById(R.id.availabilityImageView);
        root = view.findViewById(R.id.root);
    }

    public UserViewHolder(View view, boolean multiSelectEnabled, UsersListAdapter.SubtitleProvider provider) {
        super(view);
        ButterKnife.bind(this, view);
        this.provider = provider;

        this.multiSelectEnabled = multiSelectEnabled;

        avatarImageView = view.findViewById(R.id.avatarImageView);
        onlineIndicator = view.findViewById(R.id.onlineIndicator);
        nameTextView = view.findViewById(R.id.nameTextView);
        checkbox = view.findViewById(R.id.checkbox);
        statusTextView = view.findViewById(R.id.statusTextView);
        availabilityImageView = view.findViewById(R.id.availabilityImageView);
        root = view.findViewById(R.id.root);
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
