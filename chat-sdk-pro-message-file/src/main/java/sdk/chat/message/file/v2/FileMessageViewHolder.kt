package sdk.chat.message.file.v2

import android.view.View
import sdk.chat.message.file.FileMessageHolder
import sdk.chat.ui.view_holders.v2.BaseMessageViewHolder
import sdk.chat.ui.view_holders.v2.MessageDirection

open class FileMessageViewHolder<T: FileMessageHolder>(itemView: View, direction: MessageDirection): BaseMessageViewHolder<T>(itemView, direction) {

    override fun bindSendStatus(holder: T): Boolean {
        val showOverlay = super.bindSendStatus(holder)
        messageIcon?.visibility = if (showOverlay) View.INVISIBLE else View.VISIBLE
        return showOverlay
    }

}