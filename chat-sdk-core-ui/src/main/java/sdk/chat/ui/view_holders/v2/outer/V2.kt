package sdk.chat.ui.view_holders.v2.outer

import android.view.View
import sdk.chat.ui.chat.model.MessageHolder
import sdk.chat.ui.view_holders.v2.BaseMessageViewHolder
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder
import sdk.chat.ui.chat.model.ImageMessageHolder

open class V2 {
    open class IncomingMessageViewHolder(itemView: View, payload: Any?): BaseMessageViewHolder<MessageHolder>(itemView, payload)
    open class OutcomingMessageViewHolder(itemView: View, payload: Any?): BaseMessageViewHolder<MessageHolder>(itemView, payload)

    open class IncomingImageMessageViewHolder(itemView: View, payload: Any?): V2ImageMessageViewHolder<ImageMessageHolder>(itemView, payload)
    open class OutcomingImageMessageViewHolder(itemView: View, payload: Any?): V2ImageMessageViewHolder<ImageMessageHolder>(itemView, payload)
}
