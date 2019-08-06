package co.chatsdk.core.interfaces;

import co.chatsdk.core.dao.Thread;

public interface LocalNotificationHandler {
    boolean showLocalNotification(Thread thread);
}
