package sdk.chat.message.audio.v2

import android.view.View
import sdk.chat.message.audio.AudioMessageHolder
import sdk.chat.ui.view_holders.v2.MessageDirection

open class V2Audio {
    open class OutcomingMessageViewHolder(itemView: View): AudioMessageViewHolder<AudioMessageHolder>(itemView, MessageDirection.Outcoming)
    open class IncomingMessageViewHolder(itemView: View): AudioMessageViewHolder<AudioMessageHolder>(itemView, MessageDirection.Incoming)
}