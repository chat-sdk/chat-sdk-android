package sdk.chat.message.video

import android.view.View

open class V2Video {
    open class OutcomingVideoMessageViewHolder(itemView: View, payload: Any?): V2VideoMessageViewHolder<VideoMessageHolder>(itemView, payload)
    open class IncomingVideoMessageViewHolder(itemView: View, payload: Any?): V2VideoMessageViewHolder<VideoMessageHolder>(itemView, payload)
}