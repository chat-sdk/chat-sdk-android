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
import sdk.chat.core.events.EventType
import sdk.chat.core.events.NetworkEvent
import sdk.chat.core.session.ChatSDK
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.MessageHolder
import sdk.chat.ui.module.UIModule
import sdk.chat.ui.utils.DrawableUtil
import sdk.chat.ui.view_holders.base.BaseIncomingTextMessageViewHolder
import sdk.guru.common.DisposableMap
import java.text.DateFormat

open class BaseMessageViewHolder<T : MessageHolder?>(itemView: View?, payload: Any?) :
    MessageHolders.BaseMessageViewHolder<T>(itemView, payload), MessageHolders.DefaultMessageViewHolder {

    var style: MessagesListStyle? = null;

    var bubble: ViewGroup? = null

    var messageIcon: ImageView? = null
    var onlineIndicator: View? = null
    var userName: TextView? = null
    var userAvatar: CircleImageView? = null

    var imageOverlay: View? = null

    var text: TextView? = null
    var time: TextView? = null

    var readStatus: ImageView? = null
    var replyView: View? = null
    var replyImageView: ImageView? = null
    var replyTextView: TextView? = null

    var format: DateFormat? = null

    val dm = DisposableMap()

    init {

        itemView?.let {
            format = UIModule.shared().messageBinder.messageTimeComparisonDateFormat(it.context)
        }

    }

    override fun onBind(t: T) {
        bindViews()
        bindListeners(t)
        bindStyle(t)
        bind(t)
    }

    open fun bind(t: T) {
        bubble?.let {
            it.isSelected = isSelected
        }

        text?.let {
            it.text = t?.text
            it.autoLinkMask = Linkify.ALL
        }

        t?.let {
            if (replyView != null && replyTextView != null && replyImageView != null) {
                UIModule.shared().replyViewBinder.onBind(
                    replyView,
                    replyTextView,
                    replyImageView,
                    it,
                    imageLoader
                )
            }

            if (onlineIndicator != null) {
                UIModule.shared().onlineStatusBinder.bind(onlineIndicator, it)
            }

            if (time != null) {
                UIModule.shared().timeBinder.bind(time, it)

                // Hide the time if it's the same as the next message
                if (!it.showDate()) {
                    time?.visibility = View.GONE
                } else {
                    time?.visibility = View.VISIBLE
                }

            }

            bindUser(t)

            if (messageIcon != null) {
                UIModule.shared().iconBinder.bind(messageIcon, it, imageLoader)
            }

            bindReadStatus(t)

        }
    }

    open fun bindUser(t: T) {
        t?.let {
            if (userAvatar != null) {
                val pl = payload as? BaseIncomingTextMessageViewHolder.Payload
                if(pl != null) {
                    userAvatar?.setOnClickListener { _ ->
                        if (pl.avatarClickListener != null && UIModule.config().startProfileActivityOnChatViewIconClick) {
                            pl.avatarClickListener.onAvatarClick(it.message.sender)
                        }
                    }
                }

                val isAvatarExists = imageLoader != null && it.user
                    .avatar != null && it.user.avatar.isNotEmpty()

                userAvatar?.visibility = if (isAvatarExists) View.VISIBLE else View.GONE
                if (isAvatarExists) {
                    imageLoader.loadImage(userAvatar, it.user.avatar, null)
                }
            }

            if (userName != null) {
                UIModule.shared().nameBinder.bind(userName, it)
            }
        }
    }

    open fun bindReadStatus(t: T) {
        t?.let {
            if (readStatus != null) {
                UIModule.shared().readStatusViewBinder.onBind(readStatus, it)
            }
        }
    }

    open fun bindViews() {
        bubble = itemView.findViewById(R.id.bubble)

        messageIcon = itemView.findViewById(R.id.messageIcon)
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
        userName = itemView.findViewById(R.id.userName)
        userAvatar = itemView.findViewById(R.id.messageUserAvatar)

        text = itemView.findViewById(R.id.messageText)
        time = itemView.findViewById(R.id.messageTime)

        readStatus = itemView.findViewById(R.id.readStatus)
        replyView = itemView.findViewById(R.id.replyView)
        replyImageView = itemView.findViewById(R.id.replyImageView)
        replyTextView = itemView.findViewById(R.id.replyTextView)
    }

    open fun bindListeners(t: T) {
        dm.dispose()
        t?.let {
            dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageReadReceiptUpdated, EventType.MessageSendStatusUpdated))
                .filter(NetworkEvent.filterMessageEntityID(t.id))
                .subscribe {
                    bindReadStatus(t)
                })
            dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.UserPresenceUpdated, EventType.UserMetaUpdated))
                .filter(NetworkEvent.filterUserEntityID(t.user.id))
                .subscribe {
                    bindUser(t)
            })
            dm.add(ChatSDK.events().sourceOnMain()
                .filter(NetworkEvent.filterType(EventType.MessageUpdated))
                .filter(NetworkEvent.filterMessageEntityID(t.id))
                .subscribe {
                    bind(t)
                })
        }
    }

    override fun applyStyle(style: MessagesListStyle) {
        this.style = style
    }

    open fun bindStyle(t: T) {
        style?.let {
            if (t != null) {
                if (t.direction() == MessageDirection.Incoming) {
                    applyIncomingStyle(it)
                } else {
                    applyOutcomingStyle(it)
                }
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
}
