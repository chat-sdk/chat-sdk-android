package sdk.chat.message.video

import android.view.View
import sdk.chat.core.manager.DownloadablePayload
import sdk.chat.core.storage.TransferStatus
import sdk.chat.core.types.MessageSendStatus
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.ImageMessageHolder
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder

open class V2VideoMessageViewHolder<T: VideoMessageHolder>(itemView: View, payload: Any?): V2ImageMessageViewHolder<T>(itemView, payload) {

    override fun bindSendStatus(t: T) {
        super.bindSendStatus(t)

        if (t.sendStatus == MessageSendStatus.None || t.sendStatus == MessageSendStatus.Sent) {
            val payload = t.payload
            if (payload is DownloadablePayload) {
                if (payload.downloadStatus() == TransferStatus.Complete) {
                    // Then we show the play button
                    actionButton?.setBackgroundResource(R.drawable.icn_60_play)
                    actionButton?.visibility = View.VISIBLE
                    actionButton?.isClickable = false
                } else if (payload.canDownload()) {
                    actionButton?.setBackgroundResource(R.drawable.icn_60_download)
                    actionButton?.isClickable = true
                } else {
                    actionButton?.visibility = View.INVISIBLE
                }
            }
        }

    }

    override fun actionButtonPressed(holder: ImageMessageHolder) {
        val payload = holder.payload
        if (payload is DownloadablePayload) {
            if (payload.canDownload()) {
                payload.startDownload().subscribe()
            }
        }
    }

}