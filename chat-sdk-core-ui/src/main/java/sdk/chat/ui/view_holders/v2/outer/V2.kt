package sdk.chat.ui.view_holders.v2.outer

import android.view.View
import sdk.chat.ui.chat.model.ImageMessageHolder
import sdk.chat.ui.chat.model.MessageHolder
import sdk.chat.ui.view_holders.v2.BaseMessageViewHolder
import sdk.chat.ui.view_holders.v2.MessageDirection
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder

open class V2 {
    open class IncomingMessageViewHolder(itemView: View): BaseMessageViewHolder<MessageHolder>(itemView, MessageDirection.Incoming)
    open class OutcomingMessageViewHolder(itemView: View): BaseMessageViewHolder<MessageHolder>(itemView, MessageDirection.Outcoming)

    open class IncomingImageMessageViewHolder(itemView: View): V2ImageMessageViewHolder<ImageMessageHolder>(itemView, MessageDirection.Incoming)
    open class OutcomingImageMessageViewHolder(itemView: View): V2ImageMessageViewHolder<ImageMessageHolder>(itemView, MessageDirection.Outcoming)
}
