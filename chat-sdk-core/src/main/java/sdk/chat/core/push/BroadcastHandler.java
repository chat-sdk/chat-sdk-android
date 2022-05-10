package sdk.chat.core.push;

import android.content.Context;
import android.content.Intent;

public interface BroadcastHandler {
    boolean onReceive(Context context, Intent intent);
    boolean canHandle(Intent intent);
}
