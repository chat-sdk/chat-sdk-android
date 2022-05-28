package sdk.chat.message.video.v2

import android.view.View
import sdk.chat.message.video.VideoMessageHolder
import sdk.chat.ui.view_holders.v2.MessageDirection

open class V2Video {
    open class OutcomingMessageViewHolder(itemView: View): V2VideoMessageViewHolder<VideoMessageHolder>(itemView, MessageDirection.Outcoming)
    open class IncomingMessageViewHolder(itemView: View): V2VideoMessageViewHolder<VideoMessageHolder>(itemView, MessageDirection.Incoming)
}