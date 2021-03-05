package sdk.chat.ui.activities.thread.details

import sdk.chat.core.dao.Thread
import sdk.chat.core.dao.User
import sdk.chat.core.interfaces.ThreadType
import sdk.chat.core.session.ChatSDK

open class ThreadUser(public val thread: Thread, val user: User) {
    fun isActive(): Boolean {
       if (thread.typeIs(ThreadType.Group)) {
           return ChatSDK.thread().isActive(thread, user)
       } else {
           return user.isOnline
       }
    }
}