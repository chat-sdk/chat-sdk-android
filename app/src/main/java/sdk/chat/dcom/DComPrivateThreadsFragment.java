package sdk.chat.dcom;

import android.app.AlertDialog;

import sdk.chat.core.dao.ThreadX;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.fragments.PrivateThreadsFragment;

public class DComPrivateThreadsFragment extends PrivateThreadsFragment {

    public void showLongPressDialog(ThreadX thread) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setItems(new CharSequence[]{"Clear messages", "Delete conversation"}, (dialogInterface, i) -> {
            if (i == 0) {
                DCom.shared().deleteAllMessages(thread);
            } else if (i == 1) {
                ChatSDK.thread().deleteThread(thread).subscribe(this);
            }
        });

        // create alert dialog
        AlertDialog alertDialog = builder.create();

        // show it
        alertDialog.show();
    }

}
