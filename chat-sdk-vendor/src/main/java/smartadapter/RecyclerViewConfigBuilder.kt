package smartadapter

import androidx.recyclerview.widget.RecyclerView
import smartadapter.viewholder.SmartViewHolder

typealias RecyclerViewBinder = (viewHolder: SmartViewHolder<*>, recyclerView: RecyclerView) -> Unit