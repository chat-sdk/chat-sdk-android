package sdk.chat.message.video

import android.view.View
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder

open class V2Video {
    open class OutcomingVideoMessageViewHolder(itemView: View, payload: Any?): V2VideoMessageViewHolder<VideoMessageHolder>(itemView, payload)
    open class IncomingVideoMessageViewHolder(itemView: View, payload: Any?): V2ImageMessageViewHolder<VideoMessageHolder>(itemView, payload)
}