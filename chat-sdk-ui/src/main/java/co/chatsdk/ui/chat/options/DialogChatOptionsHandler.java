package co.chatsdk.ui.chat.options;

import android.app.AlertDialog;
import android.content.Context;

import java.util.List;

import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.AbstractChatOptionsHandler;
import co.chatsdk.ui.R;

/**
 * Created by ben on 10/11/17.
 */

public class DialogChatOptionsHandler extends AbstractChatOptionsHandler {

    private AlertDialog dialog;
    private boolean hasExecuted = false;

    public DialogChatOptionsHandler(ChatOptionsDelegate delegate) {
        super(delegate);
    }

    @Override
    public boolean show(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final List<ChatOption> options = ChatSDK.ui().getChatOptions();

        String [] items = new String [options.size()];
        int i = 0;

        for(ChatOption option : options) {
            items[i++] = option.getTitle();
        }

        hasExecuted = false;

        builder.setTitle(context.getString(R.string.actions)).setItems(items, (dialogInterface, i1) -> {
            if(!hasExecuted) {
                executeOption(options.get(i1));
            }
            hasExecuted = true;
        });

        dialog = builder.show();

        return true;
    }

    @Override
    public boolean hide() {
        if(dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        return false;
    }

    @Override
    public void setDelegate(ChatOptionsDelegate delegate) {

    }

    @Override
    public ChatOptionsDelegate getDelegate() {
        return null;
    }
}
