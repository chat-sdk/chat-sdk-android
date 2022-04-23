package sdk.chat.ui.view_holders;

import android.view.View;
import android.widget.ImageView;

import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.interfaces.ThreadType;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;
import sdk.chat.ui.R2;
import sdk.chat.ui.chat.model.ThreadHolder;
import sdk.chat.ui.chat.model.TypingThreadHolder;
import sdk.chat.ui.module.UIModule;
import sdk.guru.common.DisposableMap;

public class ThreadViewHolder extends DialogsListAdapter.DialogViewHolder<ThreadHolder> {

    @BindView(R2.id.onlineIndicator) protected View onlineIndicator;
    @BindView(R2.id.readStatus) protected ImageView readStatus;

    protected DisposableMap dm = new DisposableMap();
    protected TypingThreadHolder typingThreadHolder = null;

    public ThreadViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void onBind(ThreadHolder holder) {
//        holder.update();

        if (typingThreadHolder != null) {
            holder = typingThreadHolder;
        }

        super.onBind(holder);

        bindOnlineIndicator(holder);
        bindReadStatus(holder);

        addListeners(holder);
    }

    public void bindReadStatus(ThreadHolder holder) {
        UIModule.shared().getReadStatusViewBinder().onBind(readStatus, holder.getLastMessage());
    }

    public void bindOnlineIndicator(ThreadHolder holder) {
        if (holder.getThread().typeIs(ThreadType.Private1to1)) {
            onlineIndicator.setVisibility(View.VISIBLE);
            boolean isOnline = false;
            if (holder.getThread().otherUser() != null) {
                isOnline = holder.getThread().otherUser().getIsOnline();
            }
            UIModule.shared().getOnlineStatusBinder().bind(onlineIndicator, isOnline);
        } else {
            onlineIndicator.setVisibility(View.GONE);
        }
    }

    public void addListeners(ThreadHolder holder) {
        dm.dispose();

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageUpdated))
                .filter(NetworkEvent.filterThreadEntityID(holder.getId()))
                .subscribe(networkEvent -> {
                    super.onBind(holder);
        }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated))
                .filter(NetworkEvent.filterThreadEntityID(holder.getId()))
                .subscribe(networkEvent -> {
                    bindReadStatus(holder);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated))
                .filter(NetworkEvent.filterThreadContainsUser(holder.getThread()))
                .subscribe(networkEvent -> {
                    bindOnlineIndicator(holder);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserMetaUpdated))
                .filter(NetworkEvent.filterThreadContainsUser(holder.getThread()))
                .subscribe(networkEvent -> {
                    super.onBind(holder);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageAdded, EventType.MessageRemoved))
                .filter(NetworkEvent.filterThreadEntityID(holder.getId()))
                .subscribe(networkEvent -> {
                    super.onBind(holder);
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.TypingStateUpdated))
                .filter(NetworkEvent.filterThreadEntityID(holder.getId()))
                .subscribe(networkEvent -> {
                    if (networkEvent.getText() != null) {
                        String typingText = networkEvent.getText();
                        typingText += ChatSDK.getString(R.string.typing);
                        typingThreadHolder = new TypingThreadHolder(networkEvent.getThread(), typingText);
                        super.onBind(typingThreadHolder);
                    } else {
                        typingThreadHolder = null;
                        super.onBind(holder);
                    }
                }));

        dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.ThreadMetaUpdated))
                .filter(NetworkEvent.filterThreadEntityID(holder.getId()))
                .subscribe(networkEvent -> {
                    super.onBind(holder);
                }));
    }

}
