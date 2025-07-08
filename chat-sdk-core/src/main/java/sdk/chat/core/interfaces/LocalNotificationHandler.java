package sdk.chat.core.interfaces;

import sdk.chat.core.dao.ThreadX;

public interface LocalNotificationHandler {
    boolean showLocalNotification(ThreadX thread);
}
