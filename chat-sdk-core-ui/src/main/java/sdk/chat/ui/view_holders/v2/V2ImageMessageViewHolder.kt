package sdk.chat.ui.view_holders.v2

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import org.pmw.tinylog.Logger
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.ImageMessageHolder

open class V2ImageMessageViewHolder<T: ImageMessageHolder>(itemView: View, direction: MessageDirection): BaseMessageViewHolder<T>(itemView, direction) {

    open var image: ImageView? = itemView.findViewById(R.id.image)
    open var imageOverlayContainer: LinearLayout? = itemView.findViewById(R.id.imageOverlayContainer)

    override fun onBind(holder: T) {
        super.onBind(holder)

        image.let {
            loadImage(holder)
        }

        imageOverlay?.let {
            it.setImageDrawable(
                ChatSDKUI.icons().get(
                    it.context,
                    ChatSDKUI.icons().check,
                    R.color.white
                )
            )
        }

        imageOverlayContainer?.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

        bind(holder)
    }

    open fun loadImage(holder: T) {
        Logger.debug("ImageSize: " + holder.size.width + ", " + holder.size.height)
        ChatSDKUI.provider().imageLoader().load(image, holder.imageUrl, holder.placeholder(), holder.size)
    }

}