package sdk.chat.message.sticker.keyboard

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import sdk.chat.message.sticker.R
import smartadapter.viewholder.SmartViewHolder

class StickerPackViewHolder(parentView: ViewGroup):
    SmartViewHolder<StickerPackModel>(parentView, R.layout.view_holder_sticker) {

    open var imageView: ImageView = itemView.findViewById(R.id.imageView)
    open var selectedView: View = itemView.findViewById(R.id.selectedView)

    override fun bind(item: StickerPackModel) {
        itemView.layoutParams.width = item.size
        item.pack.load(imageView)
        if (item.selected) {
            selectedView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        } else {
            selectedView.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}