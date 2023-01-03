package smartadapter.viewholder

/*
 * Created by Manne Öhlund on 2019-07-23.
 * Copyright (c). All rights reserved.
 */

import androidx.recyclerview.widget.RecyclerView

/**
 * Lets you decide if a ViewHolder created by the Adapter should be recycled due to its transient state.
 * Will be called from [RecyclerView.Adapter.onFailedToRecycleView].
 */
interface RecyclableViewHolder {

    /**
     * @see RecyclerView.Adapter.onFailedToRecycleView
     * @return True if the View should be recycled, false otherwise. Note that if this method
     * returns `true`, RecyclerView *will ignore* the transient state of
     * the View and recycle it regardless. If this method returns `false`,
     * RecyclerView will check the View's transient state again before giving a final decision.
     * Default implementation returns false.
     */
    fun onFailedToRecycleView(): Boolean
}
