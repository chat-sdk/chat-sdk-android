package sdk.chat.core.push;

import android.content.Context;
import android.content.Intent;

public interface BroadcastHandler {
    void onReceive(Context context, Intent intent);
}
