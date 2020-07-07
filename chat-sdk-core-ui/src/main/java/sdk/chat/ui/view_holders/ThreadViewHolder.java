package sdk.chat.ui.view_holders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.chat.model.UserHolder;
import sdk.chat.ui.module.UIModule;

public class ThreadViewHolder extends DialogsListAdapter.DialogViewHolder<ThreadHolder> {

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.readStatus) protected ImageView readStatus;

    public ThreadViewHolder(View itemView) {
        super(itemView);
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
