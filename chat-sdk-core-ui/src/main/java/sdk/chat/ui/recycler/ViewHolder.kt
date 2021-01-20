package sdk.chat.ui.recycler

import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import sdk.chat.ui.R
import smartadapter.viewholder.SmartViewHolder

open class SimpleSelectableRadioButtonViewHolder(parentView: ViewGroup) :
        SmartViewHolder<Int>(parentView, R.layout.recycler_view_holder_radio) {

    protected var textView: TextView = itemView.findViewById(R.id.textView)
    protected var radioButton: RadioButton = itemView.findViewById(R.id.radioButton)

    override fun bind(item: Int) {
        textView.text = "Item $item"
    }
}