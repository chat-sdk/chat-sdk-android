package smartadapter.listener

import androidx.recyclerview.widget.RecyclerView

/**
 * Listener for when [smartadapter.SmartRecyclerAdapter] has been detached to the target recycler view.
 *
 * Invoked from [smartadapter.SmartRecyclerAdapter.onDetachedFromRecyclerView] and can be implemented in a [smartadapter.binders.SmartRecyclerAdapterExtension] that needs a recycler view.
 *
 * @see RecyclerView.Adapter.onDetachedFromRecyclerView
 */
interface OnDetachedFromRecyclerViewListener {

    /**
     * Called when [smartadapter.SmartRecyclerAdapter] has been detached to the target recycler view.
     *
     * @param recyclerView target recycler view
     */
    fun onDetachedFromRecyclerView(recyclerView: RecyclerView)
}