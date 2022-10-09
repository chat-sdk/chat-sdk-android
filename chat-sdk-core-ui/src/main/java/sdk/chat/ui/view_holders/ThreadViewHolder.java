package sdk.chat.ui.view_holders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.chat.model.UserHolder;
import sdk.chat.ui.module.UIModule;

public class ThreadViewHolder extends DialogsListAdapter.DialogViewHolder<ThreadHolder> {

    protected View onlineIndicator;
    protected ImageView readStatus;

    public ThreadViewHolder(View itemView) {
        super(itemView);

        onlineIndicator = itemView.findViewById(R.id.onlineIndicator);
        readStatus = itemView.findViewById(R.id.readStatus);

        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ThreadHolder dialog) {
        super.onBind(dialog);

        List<UserHolder> users = dialog.getUsers();
        if (users.size() == 1) {
            onlineIndicator.setVisibility(View.VISIBLE);
            boolean isOnline = users.get(0).isOnline();
            UIModule.shared().getOnlineStatusBinder().bind(onlineIndicator, isOnline);
        } else {
            onlineIndicator.setVisibility(View.GONE);
        }

        UIModule.shared().getReadStatusViewBinder().onBind(readStatus, dialog.getLastMessage());
    }

}
