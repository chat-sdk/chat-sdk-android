package sdk.chat.message.audio.v2

import android.view.View
import com.stfalcon.chatkit.messages.MessagesListStyle
import sdk.chat.message.audio.AudioMessageHolder
import sdk.chat.message.audio.AudioPlayerView
import sdk.chat.message.audio.R
import sdk.chat.ui.view_holders.v2.BaseMessageViewHolder
import sdk.chat.ui.view_holders.v2.MessageDirection

open class AudioMessageViewHolder<T: AudioMessageHolder>(itemView: View, direction: MessageDirection): BaseMessageViewHolder<T>(itemView, direction) {

    var audioPlayerView: AudioPlayerView? = itemView.findViewById(R.id.audioPlayerView)

    override fun onBind(t: T) {
        super.onBind(t)

        audioPlayerView?.bind(t.audioURL(), t.totalTime)

        // Bind the colors


    }

    override fun bindSendStatus(holder: T): Boolean {
        val showOverlay = super.bindSendStatus(holder)
        audioPlayerView?.bind(holder.audioURL(), holder.totalTime)
        audioPlayerView?.isEnabled = !showOverlay
        return showOverlay
    }

    override fun applyStyle(style: MessagesListStyle) {
        super.applyStyle(style)
        audioPlayerView?.let {
            if (direction == MessageDirection.Incoming) {
                it.setTintColor(style.incomingTextColor, style.incomingDefaultBubbleColor)
            } else {
                it.setTintColor(style.outcomingTextColor, style.outcomingDefaultBubbleColor)
            }
        }

//        audioPlayerView!!.buttonColor = R.color.white
//        audioPlayerView!!.sliderTrackColor = R.color.blue_light
//        audioPlayerView!!.sliderThumbColor = R.color.white
//        audioPlayerView!!.textColor = R.color.white
//        audioPlayerView!!.bind(message.audioURL(), message.getTotalTime())


    }
}