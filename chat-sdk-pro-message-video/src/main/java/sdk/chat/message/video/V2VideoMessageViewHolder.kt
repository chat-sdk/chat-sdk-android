package sdk.chat.message.video

import android.view.View
import sdk.chat.core.manager.DownloadablePayload
import sdk.chat.core.storage.TransferStatus
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.ImageMessageHolder
import sdk.chat.ui.view_holders.v2.V2ImageMessageViewHolder

open class V2VideoMessageViewHolder<T: VideoMessageHolder>(itemView: View, payload: Any?): V2ImageMessageViewHolder<T>(itemView, payload) {

    override fun bindSendStatus(t: T) {
        super.bindSendStatus(t)

        val payload = t.payload
        if (payload is DownloadablePayload) {
            if (payload.downloadStatus() == TransferStatus.Complete) {
                // Then we show the play button
                actionButton?.setBackgroundResource(R.drawable.icn_40_play)
                actionButton?.visibility = View.VISIBLE
                actionButton?.setOnClickListener(null)
            } else {
                actionButton?.setBackgroundResource(R.drawable.icn_40_download)
                actionButton?.setOnClickListener(View.OnClickListener {
                    actionButtonPressed(t)
                })
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