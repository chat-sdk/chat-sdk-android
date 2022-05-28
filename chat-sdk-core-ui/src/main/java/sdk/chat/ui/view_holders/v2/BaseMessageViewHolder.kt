package sdk.chat.ui.view_holders.v2

import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListStyle
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.functions.Consumer
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.manager.DownloadablePayload
import sdk.chat.core.session.ChatSDK
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.MessageHolder
import sdk.chat.ui.module.UIModule
import sdk.chat.ui.utils.DrawableUtil
import sdk.chat.ui.view_holders.base.BaseIncomingTextMessageViewHolder
import sdk.chat.ui.views.ProgressView
import sdk.guru.common.DisposableMap
import sdk.guru.common.RX
import java.text.DateFormat

open class BaseMessageViewHolder<T : MessageHolder>(itemView: View, direction: MessageDirection):
    MessageHolders.BaseMessageViewHolder<T>(itemView, null), MessageHolders.DefaultMessageViewHolder,
    Consumer<Throwable> {

    open var root: ConstraintLayout? = itemView.findViewById(R.id.root)
    open var bubble: ViewGroup? = itemView.findViewById(R.id.bubble)

    open var messageIcon: ImageView? = itemView.findViewById(R.id.messageIcon)
    open var onlineIndicator: View? = itemView.findViewById(R.id.onlineIndicator)
    open var userName: TextView? = itemView.findViewById(R.id.userName)
    open var userAvatar: CircleImageView? = itemView.findViewById(R.id.messageUserAvatar)

    open var imageOverlay: ImageView? = itemView.findViewById(R.id.imageOverlay)

    open var text: TextView? = itemView.findViewById(R.id.messageText)
    open var time: TextView? = itemView.findViewById(R.id.messageTime)

    open var readStatus: ImageView? = itemView.findViewById(R.id.readStatus)
    open var replyView: View? = itemView.findViewById(R.id.replyView)
    open var replyImageView: ImageView? = itemView.findViewById(R.id.replyImageView)
    open var replyTextView: TextView? = itemView.findViewById(R.id.replyTextView)

    open var progressView: ProgressView? = itemView.findViewById(R.id.progressView)
    open var bubbleOverlay: View? = itemView.findViewById(R.id.bubbleOverlay)

    open var format: DateFormat? = null

    open val dm = DisposableMap()
    open val direction: MessageDirection

    init {
        this.direction = direction

        itemView.let {
            format = UIModule.shared().messageBinder.messageTimeComparisonDateFormat(it.context)
        }
    }

    override fun onBind(holder: T) {
        bindListeners(holder)
//        bindStyle(holder)
        bind(holder)
    }

    open fun bind(t: T) {

        progressView?.actionButton?.setOnClickListener(View.OnClickListener {
            actionButtonPressed(t)
        })
        progressView?.bringToFront()

        bubble?.let {
            it.isSelected = isSelected
        }

        text?.let {
            it.text = t?.text
            it.autoLinkMask = Linkify.ALL
        }

        if (replyView != null && replyTextView != null && replyImageView != null) {
            UIModule.shared().replyViewBinder.onBind(
                replyView,
                replyTextView,
                replyImageView,
                t)
        }

        time?.let {
            UIModule.shared().timeBinder.bind(it, t)

            // Hide the time if it's the same as the next message
            it.visibility = if (t.showDate()) View.VISIBLE else View.INVISIBLE
        }

        messageIcon?.let {
            UIModule.shared().iconBinder.bind(it, t)
        }

        bindReadStatus(t)
        bindSendStatus(t)
        bindProgress(t)
        bindUser(t)
    }

    open fun bindUser(t: T) {
        userAvatar?.let {
            val pl = payload as? BaseIncomingTextMessageViewHolder.Payload
            if(pl != null) {
                it.setOnClickListener { _ ->
                    if (pl.avatarClickListener != null && UIModule.config().startProfileActivityOnChatViewIconClick) {
                        pl.avatarClickListener.onAvatarClick(t.message.sender)
                    }
                }
            }

            val isAvatarExists = imageLoader != null && t.user
                .avatar != null && t.user.avatar.isNotEmpty()

            it.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
            if (isAvatarExists) {
                ChatSDKUI.provider().imageLoader().loadSmallAvatar(it, t.user.avatar)
            }
        }

        onlineIndicator?.let {
            UIModule.shared().onlineStatusBinder.bind(it, t)
        }

        userName?.let {
            UIModule.shared().nameBinder.bind(it, t)
        }
    }

    open fun bindReadStatus(t: T) {
        readStatus?.let {
            UIModule.shared().readStatusViewBinder.onBind(it, t)
        }
    }

    open fun bindSendStatus(holder: T): Boolean {
        val showOverlay = progressView?.bindSendStatus(holder.sendStatus, holder.payload) ?: false
        bubbleOverlay?.visibility = if(showOverlay) View.VISIBLE else View.INVISIBLE
        return showOverlay
    }

    open fun bindProgress(t: T) {
        progressView?.bindProgress(t)
    }

    open fun bindListeners(t: T) {
        dm.dispose()
        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(NetworkEvent.filterType(EventType.MessageSendStatusUpdated, EventType.MessageReadReceiptUpdated))
            .filter(NetworkEvent.filterMessageEntityID(t.id))
            .doOnError(this)
            .subscribe {
                RX.main().scheduleDirect {
                    bindReadStatus(t)
                    bindSendStatus(t)
                }
            })

        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(NetworkEvent.filterType(EventType.MessageProgressUpdated))
            .filter(NetworkEvent.filterMessageEntityID(t.id))
            .doOnError(this)
            .subscribe {
                RX.main().scheduleDirect {
                    bindProgress(t)
                }
            })

        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated, EventType.UserMetaUpdated))
            .filter(NetworkEvent.filterUserEntityID(t.user.id))
            .doOnError(this)
            .subscribe {
                RX.main().scheduleDirect {
                    bindUser(t)
                }
        })
        dm.add(ChatSDK.events().sourceOnSingle()
            .filter(NetworkEvent.filterType(EventType.MessageUpdated))
            .filter(NetworkEvent.filterMessageEntityID(t.id))
            .doOnError(this)
            .subscribe {
                RX.main().scheduleDirect {
                    bind(t)
                }
            })
    }

    override fun applyStyle(style: MessagesListStyle) {
//        this.style = style
        if (direction == MessageDirection.Incoming) {
            applyIncomingStyle(style)
        } else {
            applyOutcomingStyle(style)
        }
    }

    open fun applyIncomingStyle(style: MessagesListStyle) {

        progressView?.let {
            it.setTintColor(style.incomingTextColor, style.incomingDefaultBubbleColor)
        }

        bubble?.let {
            it.setPadding(
                style.incomingDefaultBubblePaddingLeft,
                style.incomingDefaultBubblePaddingTop,
                style.incomingDefaultBubblePaddingRight,
                style.incomingDefaultBubblePaddingBottom
            )
            ViewCompat.setBackground(it, style.getIncomingBubbleDrawable())

            it.background = DrawableUtil.getMessageSelector(
                it.context,
                R.attr.incomingDefaultBubbleColor,
                R.attr.incomingDefaultBubbleSelectedColor,
                R.attr.incomingDefaultBubblePressedColor,
                R.attr.incomingBubbleDrawable
            )
        }

        text?.let {
            it.setTextColor(style.incomingTextColor)
            it.setTextSize(0, style.incomingTextSize.toFloat())
            it.setTypeface(it.typeface, style.incomingTextStyle)
            it.autoLinkMask = style.textAutoLinkMask
            it.setLinkTextColor(style.incomingTextLinkColor)
            configureLinksBehavior(it)
        }

        time?.let {
            it.setTextColor(style.incomingTimeTextColor)
            it.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                style.incomingTimeTextSize.toFloat()
            )
            it.setTypeface(it.typeface, style.incomingTimeTextStyle)
        }

        userAvatar?.let {
            it.layoutParams.width = style.incomingAvatarWidth
            it.layoutParams.height = style.incomingAvatarHeight
        }

        imageOverlay?.let {
            ViewCompat.setBackground(it, style.getIncomingImageOverlayDrawable())
        }
    }

    open fun applyOutcomingStyle(style: MessagesListStyle) {

        progressView?.let {
            it.setTintColor(style.outcomingTextColor, style.outcomingDefaultBubbleColor)
        }

        bubble?.let {
            it.setPadding(
                style.outcomingDefaultBubblePaddingLeft,
                style.outcomingDefaultBubblePaddingTop,
                style.outcomingDefaultBubblePaddingRight,
                style.outcomingDefaultBubblePaddingBottom
            )
            ViewCompat.setBackground(it, style.getOutcomingBubbleDrawable())

            it.background = DrawableUtil.getMessageSelector(
                it.context,
                R.attr.outcomingDefaultBubbleColor,
                R.attr.outcomingDefaultBubbleSelectedColor,
                R.attr.outcomingDefaultBubblePressedColor,
                R.attr.outcomingBubbleDrawable
            )
        }

        text?.let {
            it.setTextColor(style.outcomingTextColor)
            it.setTextSize(0, style.outcomingTextSize.toFloat())
            it.setTypeface(it.typeface, style.outcomingTextStyle)
            it.autoLinkMask = style.textAutoLinkMask
            it.setLinkTextColor(style.outcomingTextLinkColor)
            configureLinksBehavior(it)
        }

        time?.let {
            it.setTextColor(style.outcomingTimeTextColor)
            it.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                style.outcomingTimeTextSize.toFloat()
            )
            it.setTypeface(it.typeface, style.outcomingTimeTextStyle)
        }

        imageOverlay?.let {
            ViewCompat.setBackground(it, style.getOutcomingImageOverlayDrawable())
        }
    }

    open fun actionButtonPressed(holder: T) {
        val payload = holder.payload
        if (payload is DownloadablePayload) {
            progressView?.let { view ->
                dm.add(payload.startDownload().observeOn(RX.main()).subscribe({
                    view.actionButton?.visibility = View.INVISIBLE
                }, {
                    it.printStackTrace()
                    view.actionButton?.visibility = View.VISIBLE
                }))
            }
        }
    }

    override fun accept(t: Throwable?) {
        t?.printStackTrace()
    }
}
