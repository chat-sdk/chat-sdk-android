package sdk.chat.message.video.v2

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import sdk.chat.message.video.R
import sdk.chat.message.video.VideoMessageHolder
import sdk.chat.ui.view_holders.v2.MessageDirection
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder

open class V2VideoMessageViewHolder<T: VideoMessageHolder>(itemView: View, direction: MessageDirection): V2ImageMessageViewHolder<T>(itemView, direction) {

    open val playImageView: ImageView

    init {
        playImageView = ImageView(itemView.context)
        root?.let {
            playImageView.id = R.id.playImageView
            playImageView.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
            playImageView.setImageResource(R.drawable.icn_60_play)
            playImageView.scaleType = ImageView.ScaleType.CENTER
            it.addView(playImageView)

            val set = ConstraintSet()
            set.clone(it)
            set.connect(R.id.playImageView, ConstraintSet.START, R.id.image, ConstraintSet.START)
            set.connect(R.id.playImageView, ConstraintSet.TOP, R.id.image, ConstraintSet.TOP)
            set.connect(R.id.playImageView, ConstraintSet.END, R.id.image, ConstraintSet.END)
            set.connect(R.id.playImageView, ConstraintSet.BOTTOM, R.id.image, ConstraintSet.BOTTOM)
            set.applyTo(it)

        }
    }

    override fun bindSendStatus(t: T): Boolean {
        val showOverlay = super.bindSendStatus(t)
        playImageView.visibility = if (showOverlay) View.INVISIBLE else View.VISIBLE
        return showOverlay
    }

//    override fun actionButtonPressed(holder: T) {
//        val payload = holder.payload
//        if (payload is DownloadablePayload) {
//            if (payload.canDownload()) {
//                payload.startDownload().subscribe()
//            }
//        }
//    }

}