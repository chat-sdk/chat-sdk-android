package sdk.chat.ui.activities.thread.details

import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_view_holder_thread_user.view.*
import sdk.chat.core.defines.Availability
import sdk.chat.core.interfaces.ThreadType
import sdk.chat.core.session.ChatSDK
import sdk.chat.core.utils.Dimen
import sdk.chat.core.utils.StringChecker
import sdk.chat.ui.R
import sdk.chat.ui.binders.AvailabilityHelper
import sdk.chat.ui.module.UIModule
import smartadapter.viewholder.SmartViewHolder

class ThreadUserViewHolder(parentView: ViewGroup) :
        SmartViewHolder<ThreadUser>(parentView, R.layout.recycler_view_holder_thread_user) {

    override fun bind(item: ThreadUser) {
        with(itemView) {
            val user = item.user
            val thread = item.thread

            nameTextView.text = user.name
            if (thread.typeIs(ThreadType.Group)) {
                if (ChatSDK.thread().isActive(thread, user)) {
                    setAvailability(Availability.Available)
                } else {
                    setAvailability(Availability.Unavailable)
                }

                if (ChatSDK.thread().rolesEnabled(thread)) {
                    val role = ChatSDK.thread().roleForUser(thread, user)
                    if (role != null) {
                        statusTextView.text = ChatSDK.thread().localizeRole(role)
                    } else {
                        statusTextView.text = ""
                    }
                }

            } else {
                setAvailability(user.availability)
                statusTextView.text = user.status
            }

            UIModule.shared().onlineStatusBinder.bind(onlineIndicator, user.isOnline)

            val context = ChatSDK.ctx()

            val width = Dimen.from(context, R.dimen.small_avatar_width)
            val height = Dimen.from(context, R.dimen.small_avatar_height)

            val avatarURL: String = user.avatarURL
            if (!StringChecker.isNullOrEmpty(avatarURL)) {
                Glide.with(root)
                        .load(avatarURL)
                        .dontAnimate()
                        .placeholder(UIModule.config().defaultProfilePlaceholder)
                        .override(width, height)
                        .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(UIModule.config().defaultProfilePlaceholder)
            }
        }
    }

    fun setAvailability(availability: String?) {
        with(itemView) {
            if (availability == null) {
                availabilityImageView.visibility = View.INVISIBLE
            } else {
                availabilityImageView.visibility = View.VISIBLE
                availabilityImageView.setImageResource(AvailabilityHelper.imageResourceIdForAvailability(availability))
            }
        }
    }

}