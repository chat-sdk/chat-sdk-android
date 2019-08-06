package co.chatsdk.ui.chat.options;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.lang.ref.WeakReference;
import java.util.List;

import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.R;

public class FloatingChatOptionsHandler extends AbstractChatOptionsHandler {

    protected SpeedDialView speedDialView;
    protected WeakReference<Activity> activity;

    public FloatingChatOptionsHandler(ChatOptionsDelegate delegate) {
        super(delegate);
    }

    @Override
    public boolean show(Activity activity) {
        this.activity = new WeakReference<>(activity);

        View mainView = activity.getLayoutInflater().inflate(R.layout.view_chat_options_floating, null);
        speedDialView = mainView.findViewById(R.id.speed_dial);

        activity.addContentView(mainView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final List<ChatOption> options = ChatSDK.ui().getChatOptions();

        int i = 0;
        for (ChatOption option : options) {
            speedDialView.addActionItem(new SpeedDialActionItem.Builder(i++, option.getIconResourceId())
                    .setLabel(option.getTitle())
                    .create());
        }
        speedDialView.setOnActionSelectedListener(actionItem -> {
            ChatOption option = options.get(actionItem.getId());
            //
            return false;
        });
        speedDialView.open();

        return false;
    }

    @Override
    public boolean hide() {
//        speedDialView.getParent().remo
        return false;
    }

}
