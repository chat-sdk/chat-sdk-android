package smartadapter.listener

/*
 * Created by Manne Öhlund on 21/09/20.
 * Copyright © 2020. All rights reserved.
 */

import androidx.recyclerview.widget.RecyclerView
import smartadapter.viewholder.SmartViewHolder

/**
 * Listener for when a view created by the adapter has been attached to a window.
 *
 * Invoked from [smartadapter.SmartRecyclerAdapter.onViewAttachedToWindow] and should be implemented in a [SmartViewHolder] extension.
 *
 * @see RecyclerView.Adapter.onViewAttachedToWindow
 */
interface OnViewAttachedToWindowListener {

    /**
     * Called when a view created by the adapter has been attached to a window.
     * @param viewHolder target ViewHolder
     */
    fun onViewAttachedToWindow(viewHolder: RecyclerView.ViewHolder)
}
