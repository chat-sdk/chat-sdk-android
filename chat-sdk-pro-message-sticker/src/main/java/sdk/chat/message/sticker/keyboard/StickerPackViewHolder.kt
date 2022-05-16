package sdk.chat.message.sticker.keyboard

import android.view.ViewGroup
import android.widget.ImageView
import sdk.chat.message.sticker.R
import smartadapter.viewholder.SmartViewHolder

class StickerPackViewHolder(parentView: ViewGroup):
    SmartViewHolder<StickerPackModel>(parentView, R.layout.view_holder_sticker) {

    open var imageView: ImageView = itemView.findViewById(R.id.imageView)

    override fun bind(item: StickerPackModel) {
        itemView.layoutParams.width = item.size
        item.pack.load(imageView)
    }
}