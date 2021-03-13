package smartadapter.listener

import androidx.recyclerview.widget.RecyclerView

/**
 * Listener for when [smartadapter.SmartRecyclerAdapter] has been attached to the target recycler view.
 *
 * Invoked from [smartadapter.SmartRecyclerAdapter.onAttachedToRecyclerView] and can be implemented in a [smartadapter.binders.SmartRecyclerAdapterExtension] that needs a recycler view.
 *
 * @see RecyclerView.Adapter.onAttachedToRecyclerView
 */
interface OnAttachedToRecyclerViewListener {

    /**
     * Called when [smartadapter.SmartRecyclerAdapter] has been attached to the target recycler view.
     *
     * @param recyclerView target recycler view
     */
    fun onAttachedToRecyclerView(recyclerView: RecyclerView)
}