package smartadapter.listener

/*
 * Created by Manne Öhlund on 21/09/20.
 * Copyright © 2020. All rights reserved.
 */

import androidx.recyclerview.widget.RecyclerView
import smartadapter.SmartRecyclerAdapter
import smartadapter.viewholder.SmartViewHolder

/**
 * Listener for when a view holder created by the adapter should be bound with data.
 *
 * Invoked from [smartadapter.SmartRecyclerAdapter.onCreateViewHolder] and should be implemented in a [SmartViewHolder] extension.
 *
 * @see RecyclerView.Adapter.onBindViewHolder
 */
interface OnCreateViewHolderListener {

    /**
     * Called when a view holder created by the adapter should be bound with data.
     * @param adapter target SmartRecyclerAdapter
     * @param viewHolder target ViewHolder
     */
    fun onCreateViewHolder(
        adapter: SmartRecyclerAdapter,
        viewHolder: SmartViewHolder<Any>
    )
}
