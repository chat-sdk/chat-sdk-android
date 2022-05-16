package sdk.chat.message.sticker.keyboard

import android.view.ViewGroup
import android.widget.ImageView
import sdk.chat.message.sticker.R
import smartadapter.viewholder.SmartViewHolder

class StickerViewHolder(parentView: ViewGroup):
    SmartViewHolder<StickerModel>(parentView, R.layout.view_holder_sticker) {

    open var imageView: ImageView = itemView.findViewById(R.id.imageView)

    override fun bind(item: StickerModel) {
        itemView.layoutParams.width = item.size
        item.sticker.load(imageView)
    }

}