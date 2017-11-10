package co.chatsdk.core.interfaces;

import android.content.Context;

/**
 * Created by ben on 10/11/17.
 */

public interface ChatOptionsHandler {

    boolean show (Context context);
    boolean hide ();
    void setDelegate (ChatOptionsDelegate delegate);
    ChatOptionsDelegate getDelegate ();

}
