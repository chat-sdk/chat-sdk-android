package sdk.chat.message.sticker.keyboard

import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_holder_sticker.view.*
import sdk.chat.message.sticker.R
import smartadapter.viewholder.SmartViewHolder

class StickerPackViewHolder(parentView: ViewGroup):
    SmartViewHolder<StickerPackModel>(parentView, R.layout.view_holder_sticker) {

    override fun bind(item: StickerPackModel) {
        with(itemView) {
            itemView.layoutParams.width = item.size
            item.pack.load(imageView)
        }
    }
}