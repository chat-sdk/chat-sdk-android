package smartadapter.viewholder

/*
 * Created by Manne Öhlund on 2019-07-27.
 * Copyright (c) All rights reserved.
 */

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar

import androidx.annotation.LayoutRes
import androidx.appcompat.widget.AppCompatButton

import io.github.manneohlund.smartrecycleradapter.R

/**
 * Default implementation of load more view holder.
 */
class LoadMoreViewHolder(
    parentView: View,
    @param:LayoutRes private val loadingViewRes: Int,
    isAutoLoadEnabled: Boolean
) : SmartViewHolder<Any>(
    LayoutInflater.from(parentView.context)
        .inflate(
            loadingViewRes,
            parentView as ViewGroup,
            false
        )
) {

    val loadMoreButton: AppCompatButton? = itemView.findViewById(R.id.loadMoreButton)
    val progressBar: ProgressBar? = itemView.findViewById(R.id.progressBar)

    init {
        toggleLoading(isAutoLoadEnabled)
    }

    fun toggleLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar?.visibility = View.VISIBLE
            loadMoreButton?.visibility = View.GONE
        } else {
            progressBar?.visibility = View.INVISIBLE
            loadMoreButton?.visibility = View.VISIBLE
        }
    }

    override fun bind(item: Any) {
        // Noop
    }
}
