package smartadapter

/*
 * Created by Manne Öhlund on 2019-07-27.
 * Copyright (c) All rights reserved.
 */

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import io.github.manneohlund.smartrecycleradapter.R
import smartadapter.listener.OnLoadMoreListener
import smartadapter.viewholder.LoadMoreViewHolder
import smartadapter.viewholder.SmartViewHolder

/**
 * Enables endless scrolling or pagination. Let's the adapter show a [LoadMoreViewHolder] when scrolled to last item.
 */
@Suppress("UNCHECKED_CAST")
class SmartEndlessScrollRecyclerAdapter(items: MutableList<Any>) : SmartRecyclerAdapter(items), ISmartEndlessScrollRecyclerAdapter {

    private val VIEW_TYPE_LOADING = Integer.MAX_VALUE

    private val endlessScrollOffset: Int
        get() = if (isEndlessScrollEnabled) 1 else 0

    override var isEndlessScrollEnabled: Boolean = true
        set(enable) {
            field = enable
            smartNotifyItemChanged(itemCount)
        }
    override var isLoading: Boolean = false
    override var autoLoadMoreEnabled: Boolean = false
    override var onLoadMoreListener: OnLoadMoreListener? = null
    @LayoutRes
    override var loadMoreLayoutResource = R.layout.load_more_view

    override fun getItemViewType(position: Position): ViewType {
        return if (isEndlessScrollEnabled && position == itemCount - endlessScrollOffset) {
            VIEW_TYPE_LOADING
        } else super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: ViewType): SmartViewHolder<Any> {
        return if (viewType == VIEW_TYPE_LOADING) {
            LoadMoreViewHolder(parent, loadMoreLayoutResource, autoLoadMoreEnabled)
        } else super.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(smartViewHolder: SmartViewHolder<Any>, position: Position) {
        if (position < itemCount - endlessScrollOffset) {
            super.onBindViewHolder(smartViewHolder, position)
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + endlessScrollOffset
    }

    override fun onViewAttachedToWindow(holder: SmartViewHolder<Any>) {
        super.onViewAttachedToWindow(holder)
        if (holder is LoadMoreViewHolder) {
            if (autoLoadMoreEnabled) {
                holder.itemView.post {
                    onLoadMoreListener?.invoke(this, holder)
                }
            } else {
                holder.toggleLoading(false)
                holder.loadMoreButton?.setOnClickListener {
                    holder.itemView.post {
                        onLoadMoreListener?.invoke(this, holder)
                        holder.toggleLoading(true)
                    }
                }
            }
        }
    }

    companion object {

        /**
         * Builder of [SmartRecyclerAdapter] for easy implementation.
         * @return SmartAdapterBuilder
         */
        fun items(items: List<Any>): SmartEndlessScrollAdapterBuilder =
            SmartEndlessScrollAdapterBuilder().also {
                it.setItems(items)
            }

        /**
         * Builder of [SmartRecyclerAdapter] for easy implementation.
         * @return SmartAdapterBuilder
         */
        fun empty(): SmartEndlessScrollAdapterBuilder = SmartEndlessScrollAdapterBuilder()
    }
}
