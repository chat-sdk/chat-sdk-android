package sdk.chat.ui.view_holders.v2

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import org.pmw.tinylog.Logger
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.manager.DownloadablePayload
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.types.MessageSendStatus
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.ImageMessageHolder
import sdk.chat.ui.icons.Icons
import sdk.guru.common.RX

open class V2ImageMessageViewHolder<T: ImageMessageHolder>(itemView: View, payload: Any?): BaseMessageViewHolder<T>(itemView, payload) {

    var image: ImageView? = itemView.findViewById(R.id.image)
    var imageOverlayContainer: LinearLayout? = itemView.findViewById(R.id.imageOverlayContainer)
    var bubbleOverlay: View? = itemView.findViewById(R.id.bubbleOverlay)
    var actionButton: Button? = itemView.findViewById(R.id.actionButton)
    var progressBar: CircularProgressBar? = itemView.findViewById(R.id.circularProgressBar)
    var progressText: TextView? = itemView.findViewById(R.id.progressText)

    override fun onBind(holder: T) {
        super.onBind(holder)

        actionButton?.setOnClickListener(View.OnClickListener {
            actionButtonPressed(holder)
        })

        image.let {
            Logger.debug("ImageSize: " + holder.size.width + ", " + holder.size.height)
            ChatSDKUI.provider().imageLoader().load(image, holder.imageUrl, holder.placeholder(), holder.size)
        }

        imageOverlay?.setImageDrawable(
            Icons.get(
                imageOverlay!!.context,
                ChatSDKUI.icons().check,
                R.color.white
            )
        )

        imageOverlayContainer?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

        bind(holder)
    }

    override fun bindListeners(t: T) {
        super.bindListeners(t)

        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(NetworkEvent.filterType(EventType.MessageProgressUpdated))
            .filter(NetworkEvent.filterMessageEntityID(t.id))
            .doOnError(this)
            .subscribe {
                RX.main().scheduleDirect {
                    bindProgress(t)
                }
            })
    }

    override fun bindProgress(t: T) {
        super.bindProgress(t)

        val transferInProgress = t.uploadPercentage > 0 && t.uploadPercentage <= 100

        progressBar?.let {
            it.visibility =  if(transferInProgress) View.VISIBLE else View.INVISIBLE
            it.progress = t.uploadPercentage
        }

        progressText?.let {
            it.visibility =  if (transferInProgress) View.VISIBLE else View.INVISIBLE
            it.text = String.format(ChatSDK.getString(R.string.__percent), t.uploadPercentage)
        }

        if (transferInProgress) {
            actionButton?.visibility = View.INVISIBLE
        }

    }

    override fun bindSendStatus(t: T) {
        super.bindSendStatus(t)

        var showOverlay = false

        val sendStatus = t.sendStatus
        val payload = t.payload

        actionButton?.visibility = View.INVISIBLE

        // It is uploading
        if (sendStatus == MessageSendStatus.Uploading) {
            showOverlay = true
        }
        else if (sendStatus == MessageSendStatus.Sent || sendStatus == MessageSendStatus.None) {
            if (payload is DownloadablePayload) {
                if (payload.canDownload()) {
                    // Show Overlay and download button
                    showOverlay = true

                    progressBar?.visibility = View.VISIBLE
                    actionButton?.visibility = View.VISIBLE
                    if (payload.size() != null) {
                        progressText?.visibility = View.VISIBLE

                        var size = payload.size() / 1000f
                        if (size < 1000) {
                            progressText?.text = String.format(ChatSDK.getString(R.string.__kb), size)
                        } else {
                            size /= 1000f
                            progressText?.text = String.format(ChatSDK.getString(R.string.__mb), size)
                        }
                    }
                }
            }
        }

        if (showOverlay) {
            bubbleOverlay?.visibility = View.VISIBLE
        } else {
            bubbleOverlay?.visibility = View.INVISIBLE
            progressBar?.visibility = View.INVISIBLE
            progressText?.visibility = View.INVISIBLE
            actionButton?.visibility = View.INVISIBLE
        }
    }

    open fun actionButtonPressed(holder: ImageMessageHolder) {
        val payload = holder.payload
        if (payload is DownloadablePayload) {
            if (payload.canDownload()) {
                progressBar?.progress = 1f
                dm.add(payload.startDownload().subscribe({
                   actionButton?.visibility = View.INVISIBLE
                }, {

                }))
            }
        }
    }

}