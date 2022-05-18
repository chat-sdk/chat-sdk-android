package sdk.chat.ui.view_holders.v2

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import sdk.chat.ui.ChatSDKUI
import sdk.chat.ui.R
import sdk.chat.ui.chat.model.ImageMessageHolder
import sdk.chat.ui.icons.Icons
import sdk.chat.ui.utils.ImageLoaderPayload

class V2ImageMessageViewHolder(itemView: View?, payload: Any?): BaseMessageViewHolder<ImageMessageHolder>(itemView, payload) {

    var image: ImageView? = itemView?.findViewById(R.id.image)
    var imageOverlayContainer: LinearLayout? = itemView?.findViewById(R.id.imageOverlayContainer)

    override fun onBind(holder: ImageMessageHolder) {
        super.onBind(holder)

        // Check to see if we have a placeholder
        val placeholder = holder.placeholder()
        if (placeholder != null) {
            image?.setImageBitmap(placeholder)
        } else {

        }

        if (image != null && imageLoader != null) {
            // TODO:
            imageLoader.loadImage(image, holder.imageUrl, getPayloadForImageLoader(holder))
        }

        imageOverlay?.setImageDrawable(
            Icons.get(
                imageOverlay!!.context,
                ChatSDKUI.icons().check,
                R.color.white
            )
        )

        if (imageOverlayContainer != null) {
            imageOverlayContainer!!.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        }

//        UIModule.shared().onlineStatusBinder.bind(onlineIndicator, holder.user.isOnline)
//        UIModule.shared().nameBinder.bind(userName, holder)
//        UIModule.shared().timeBinder.bind(time, holder)
    }

//    override fun bindViews() {
//        super.bindViews()
//        image = itemView.findViewById(R.id.image)
//        imageOverlayContainer = itemView.findViewById(R.id.imageOverlayContainer)
//    }

    /**
     * Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
     *
     * @param message Message with image
     */
    fun getPayloadForImageLoader(message: ImageMessageHolder): Any? {
        return ImageLoaderPayload(message.defaultPlaceholder())
    }

}