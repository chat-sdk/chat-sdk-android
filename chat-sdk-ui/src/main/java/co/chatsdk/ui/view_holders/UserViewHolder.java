package co.chatsdk.ui.view_holders;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.pmw.tinylog.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.R2;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.binders.AvailabilityHelper;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder  {

    protected boolean multiSelectEnabled;

    @BindView(R2.id.avatarImageView) protected CircleImageView avatarImageView;
    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.nameTextView) protected TextView nameTextView;
    @BindView(R2.id.checkbox) protected CheckBox checkbox;
    @BindView(R2.id.statusTextView) protected TextView statusTextView;
    @BindView(R2.id.availabilityImageView) protected ImageView availabilityImageView;
    @BindView(R2.id.root) protected RelativeLayout root;

    public UserViewHolder(View view, boolean multiSelectEnabled) {
        super(view);
        ButterKnife.bind(this, view);

        this.multiSelectEnabled = multiSelectEnabled;

        // Clicks are handled at the list item level
        checkbox.setClickable(false);
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

        OnlineStatusBinder.bind(onlineIndicator, item.getIsOnline());

        statusTextView.setText(item.getStatus());

        Logger.debug("User: " + item.getName() + " Availability: " + item.getAvailability());

        Context context = ChatSDK.shared().context();

        int width = Dimen.from(context, R.dimen.small_avatar_width);
        int height = Dimen.from(context, R.dimen.small_avatar_height);

        if (item instanceof User) {
            ((User) item).loadAvatar(avatarImageView, width, height);
        } else {
            Picasso.get().load(item.getAvatarURL()).resize(width, height).into(avatarImageView);
        }
    }

    public void setChecked(boolean checked) {
        checkbox.setChecked(checked);
    }

}
