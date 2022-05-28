package sdk.chat.message.file.v2

import android.view.View
import sdk.chat.message.file.FileMessageHolder
import sdk.chat.ui.view_holders.v2.MessageDirection

class V2File {
    open class OutcomingMessageViewHolder(itemView: View): FileMessageViewHolder<FileMessageHolder>(itemView, MessageDirection.Outcoming)
    open class IncomingMessageViewHolder(itemView: View): FileMessageViewHolder<FileMessageHolder>(itemView, MessageDirection.Incoming)
}