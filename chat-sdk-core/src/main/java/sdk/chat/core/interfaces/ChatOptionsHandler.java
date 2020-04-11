package sdk.chat.core.interfaces;

import android.app.Activity;

/**
 * Created by ben on 10/11/17.
 */

public interface ChatOptionsHandler {

    boolean show(Activity activity);
    boolean hide();
    void setDelegate(ChatOptionsDelegate delegate);
    ChatOptionsDelegate getDelegate();

}
