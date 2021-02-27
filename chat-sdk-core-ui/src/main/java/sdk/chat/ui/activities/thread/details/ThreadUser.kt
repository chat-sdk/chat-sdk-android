package sdk.chat.ui.activities.thread.details

import sdk.chat.core.dao.Thread
import sdk.chat.core.dao.User
import sdk.chat.core.interfaces.ThreadType

open class ThreadUser(public val thread: Thread, val user: User) {
    fun isActive(): Boolean {
       if (thread.typeIs(ThreadType.Group)) {
           return thread.getUserThreadLink(user.id)?.isActive ?: false
       } else {
           return user.isOnline
       }
    }
}