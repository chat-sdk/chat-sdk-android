package sdk.chat.ui.keyboard

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import sdk.chat.ui.R
import sdk.chat.ui.module.UIModule
import smartadapter.viewholder.SmartViewHolder

class ChatOptionViewHolder(parentView: ViewGroup):
    SmartViewHolder<ChatOptionModel>(parentView, R.layout.recycler_view_holder_chat_option) {

    open var textView: TextView = itemView.findViewById(R.id.textView)
    open var imageView: ImageView = itemView.findViewById(R.id.imageView)

    override fun bind(item: ChatOptionModel) {
        with(itemView) {

            item.width?.let {
                itemView.layoutParams.width = it
            }

            item.height?.let {
                itemView.layoutParams.height = it
            }

//            itemView.layoutParams.width = item.size
            textView.text = item.title
            imageView.setImageResource(item.imageRes)


            val filter = PorterDuffColorFilter(ContextCompat.getColor(context, UIModule.config().chatOptionIconColor), PorterDuff.Mode.MULTIPLY)
            imageView.drawable.colorFilter = filter

        }
    }

}