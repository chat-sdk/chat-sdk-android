package co.chatsdk.ui.chat.options;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import java.util.List;

import co.chatsdk.ui.AbstractChatOptionsHandler;
import co.chatsdk.core.interfaces.ChatOption;
import co.chatsdk.core.interfaces.ChatOptionsDelegate;
import co.chatsdk.ui.manager.InterfaceManager;

/**
 * Created by ben on 10/11/17.
 */

public class DialogChatOptionsHandler extends AbstractChatOptionsHandler {

    private AlertDialog dialog;

    public DialogChatOptionsHandler(ChatOptionsDelegate delegate) {
        super(delegate);
    }

    @Override
    public boolean show(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        final List<ChatOption> options = InterfaceManager.shared().a.getChatOptions();

        String [] items = new String [options.size()];
        int i = 0;

        for(ChatOption option : options) {
            items[i++] = option.getTitle();
        }

        // TODO: Localize
        builder.setTitle("Actions").setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeOption(options.get(i));
            }
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
