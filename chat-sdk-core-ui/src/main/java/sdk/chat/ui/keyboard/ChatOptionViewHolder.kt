package sdk.chat.ui.keyboard

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.recycler_view_holder_chat_option.view.*
import kotlinx.android.synthetic.main.recycler_view_holder_section.view.textView
import sdk.chat.ui.R
import sdk.chat.ui.module.UIModule
import smartadapter.viewholder.SmartViewHolder

class ChatOptionViewHolder(parentView: ViewGroup):
    SmartViewHolder<ChatOptionModel>(parentView, R.layout.recycler_view_holder_chat_option) {

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