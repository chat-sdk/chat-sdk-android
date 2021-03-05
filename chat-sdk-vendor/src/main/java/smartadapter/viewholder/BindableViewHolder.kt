package smartadapter.viewholder

/*
 * Created by Manne Öhlund on 2019-07-21.
 * Copyright (c) All rights reserved.
 */

import androidx.recyclerview.widget.RecyclerView

/**
 * Contains basic logic methods [bind] and [unbind] for the [SmartViewHolder].
 * @param <T> Data item
 */
interface BindableViewHolder<in T : Any> {

    /**
     * Called when a [SmartViewHolder] is created or recycled from [RecyclerView.Adapter.onBindViewHolder]
     * @param item data
     */
    fun bind(item: T)

    /**
     * Called when a [SmartViewHolder] is created or recycled from [RecyclerView.Adapter.onBindViewHolder]
     * @param item data
     */
    fun bind(item: T, payloads: MutableList<Any>) {
        // No op
    }

    /**
     * Called when [SmartViewHolder] is recycled in [RecyclerView.Adapter.onViewRecycled].
     * Default implementation has no operation.
     */
    fun unbind() {
        // No op
    }
}
