package sdk.chat.message.sticker.keyboard

import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_holder_sticker.view.*
import sdk.chat.message.sticker.R
import smartadapter.viewholder.SmartViewHolder

class StickerViewHolder(parentView: ViewGroup):
    SmartViewHolder<StickerModel>(parentView, R.layout.view_holder_sticker) {

    override fun bind(item: StickerModel) {
        with(itemView) {
            itemView.layoutParams.width = item.size
            item.sticker.load(imageView)
//            imageView.setImageResource(item.sticker.image)
        }
    }

}