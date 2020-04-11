package sdk.chat.core.interfaces;

import sdk.chat.core.dao.Thread;

public interface LocalNotificationHandler {
    boolean showLocalNotification(Thread thread);
}
