package sdk.chat.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.annotations.NonNull;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.MessageMetaValue;
import sdk.chat.core.dao.ReadReceiptUserLink;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.dao.ThreadMetaValue;
import sdk.chat.core.dao.UserThreadLink;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.R;

public class DebugFragment extends BaseFragment {

    Button deleteThreadsButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        deleteThreadsButton = view.findViewById(R.id.deleteThreadsButton);

        initViews();
        return view;
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_debug;
    }

    @Override
    protected void initViews() {

        deleteThreadsButton.setOnClickListener(v -> {
            List<Object> toDelete = new ArrayList<>();

            toDelete.addAll(ChatSDK.db().getDaoCore().fetchEntitiesOfClass(Thread.class));
            toDelete.addAll(ChatSDK.db().getDaoCore().fetchEntitiesOfClass(Message.class));
            toDelete.addAll(ChatSDK.db().getDaoCore().fetchEntitiesOfClass(UserThreadLink.class));
            toDelete.addAll(ChatSDK.db().getDaoCore().fetchEntitiesOfClass(ThreadMetaValue.class));
            toDelete.addAll(ChatSDK.db().getDaoCore().fetchEntitiesOfClass(MessageMetaValue.class));
            toDelete.addAll(ChatSDK.db().getDaoCore().fetchEntitiesOfClass(ReadReceiptUserLink.class));

            for (Object o: toDelete) {
                ChatSDK.db().delete(o);
                if (o instanceof Thread) {
                    ChatSDK.events().source().accept(NetworkEvent.threadRemoved((Thread) o));
                }
                if (o instanceof Message) {
                    ChatSDK.events().source().accept(NetworkEvent.messageRemoved((Message) o));
                }
            }

        });

    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }
}
