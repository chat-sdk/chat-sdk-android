package co.chatsdk.ui.view_holders;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.pmw.tinylog.Logger;

import co.chatsdk.core.dao.User;
import co.chatsdk.core.interfaces.UserListItem;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.Dimen;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.binders.OnlineStatusBinder;
import co.chatsdk.ui.databinding.ViewUserRowBinding;
import co.chatsdk.ui.binders.AvailabilityHelper;

public class UserViewHolder extends RecyclerView.ViewHolder  {

    protected ViewUserRowBinding b;
    protected boolean multiSelectEnabled;

    public UserViewHolder(View view, ViewUserRowBinding binding, boolean multiSelectEnabled) {
        super(view);
        b = binding;
        this.multiSelectEnabled = multiSelectEnabled;

        // Clicks are handled at the list item level
        b.checkbox.setClickable(false);
    }

    public void bind(UserListItem item) {

        b.nameTextView.setText(item.getName());

        if (multiSelectEnabled) {
            b.checkbox.setVisibility(View.VISIBLE);
        } else {
            b.checkbox.setVisibility(View.INVISIBLE);
        }

        if (StringChecker.isNullOrEmpty(item.getAvailability()) || multiSelectEnabled) {
            b.availabilityImageView.setVisibility(View.INVISIBLE);
        } else {
            b.availabilityImageView.setVisibility(View.VISIBLE);
            b.availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(item.getAvailability()));
        }

        OnlineStatusBinder.bind(b.onlineIndicator, item.getIsOnline());

        b.statusTextView.setText(item.getStatus());

        Logger.debug("User: " + item.getName() + " Availability: " + item.getAvailability());

        Context context = ChatSDK.shared().context();

        int width = Dimen.from(context, R.dimen.small_avatar_width);
        int height = Dimen.from(context, R.dimen.small_avatar_height);

        if (item instanceof User) {
            ((User) item).loadAvatar(b.avatarImageView, width, height);
        } else {
            Picasso.get().load(item.getAvatarURL()).resize(width, height).into(b.avatarImageView);
        }
    }

    public void setChecked(boolean checked) {
        b.checkbox.setChecked(checked);
    }

}
