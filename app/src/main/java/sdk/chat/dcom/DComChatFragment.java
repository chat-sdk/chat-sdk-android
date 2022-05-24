package sdk.chat.dcom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.reactivex.annotations.NonNull;
import sdk.chat.core.events.EventType;
import sdk.chat.core.events.NetworkEvent;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.fragments.ChatFragment;

public class DComChatFragment extends ChatFragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        dm.add(ChatSDK.events().sourceOnMain().filter(NetworkEvent.filterType(EventType.MessageUpdated)).subscribe(networkEvent -> {
            // This can be expensive and this method can be called a lot... so make sure the reload data key is also set
            if (networkEvent.getMessage() != null && networkEvent.getData() != null && networkEvent.getData().containsKey(DCom.reloadData)) {
                chatView.post(() -> {
//                    chatView.reloadMessage(networkEvent.getMessage());
                });
            }
        }));



        return rootView;
    }

}
