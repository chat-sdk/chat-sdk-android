package sdk.chat.ui.view_holders.v2

import android.text.util.Linkify
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.stfalcon.chatkit.messages.MessageHolders
import com.stfalcon.chatkit.messages.MessagesListStyle
import de.hdodenhof.circleimageview.CircleImageView
import io.reactivex.functions.Consumer
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.session.ChatSDK
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.MessageHolder
import sdk.chat.ui.module.UIModule
import sdk.chat.ui.utils.DrawableUtil
import sdk.chat.ui.view_holders.base.BaseIncomingTextMessageViewHolder
import sdk.guru.common.DisposableMap
import sdk.guru.common.RX
import java.text.DateFormat

open class BaseMessageViewHolder<T : MessageHolder>(itemView: View, payload: Any?) :
    MessageHolders.BaseMessageViewHolder<T>(itemView, payload), MessageHolders.DefaultMessageViewHolder,
    Consumer<Throwable> {

    open var style: MessagesListStyle? = null;

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

    open var format: DateFormat? = null

    open val dm = DisposableMap()

    init {

        itemView.let {
            format = UIModule.shared().messageBinder.messageTimeComparisonDateFormat(it.context)
        }

    }

    override fun onBind(holder: T) {
        bindListeners(holder)
        bindStyle(holder)
        bind(holder)
    }

    open fun bind(t: T) {
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
        bindProgress(t)
        bindSendStatus(t)
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
                ChatSDKUI.provider().imageLoader().loadAvatar(it, t.user.avatar)
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

    open fun bindSendStatus(t: T) {
    }

    open fun bindProgress(t: T) {

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
        this.style = style
    }

    open fun bindStyle(t: T) {
        style?.let {
            if (t.direction() == MessageDirection.Incoming) {
                applyIncomingStyle(it)
            } else {
                applyOutcomingStyle(it)
            }
        }
    }

    open fun applyIncomingStyle(style: MessagesListStyle) {
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
            it.setTextColor(style.incomingImageTimeTextColor)
            it.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                style.incomingImageTimeTextSize.toFloat()
            )
            it.setTypeface(it.typeface, style.incomingImageTimeTextStyle)
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
            it.setTextColor(style.outcomingImageTimeTextColor)
            it.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                style.outcomingImageTimeTextSize.toFloat()
            )
            it.setTypeface(it.typeface, style.outcomingImageTimeTextStyle)
        }

        imageOverlay?.let {
            ViewCompat.setBackground(it, style.getOutcomingImageOverlayDrawable())
        }
    }

    override fun accept(t: Throwable?) {
        t?.printStackTrace()
    }
}
