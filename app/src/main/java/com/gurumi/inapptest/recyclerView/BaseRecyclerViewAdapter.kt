package com.droidcba.kedditbysteps.commons.recyclerView

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.gurumi.inapptest.recyclerView.adapter.AdapterConstants
import com.droidcba.kedditbysteps.commons.recyclerView.adapter.ViewType
import com.droidcba.kedditbysteps.commons.recyclerView.adapter.ViewTypeDelegateAdapter
import java.util.*

/**
 *
 * Create : Kwon IkYoung
 * Date : 2018. 1. 17.
 */

abstract class BaseRecyclerViewAdapter(var isLoading: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    protected val nullItems = object : ViewType {
        override fun getViewType() = AdapterConstants.NULL
    }

    private val loadingItem = object : ViewType {
        override fun getViewType() = AdapterConstants.LOADING
    }

    protected var items: ArrayList<ViewType> = ArrayList()

    protected var delegateAdapters = SparseArrayCompat<ViewTypeDelegateAdapter>()

    init {
        if (isLoading) {
            items.add(loadingItem)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = delegateAdapters.get(viewType).onCreateViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = delegateAdapters.get(getItemViewType(position)).onBindViewHolder(holder, items[position])

    override fun getItemViewType(position: Int) = items.getOrNull(position)?.getViewType()
            ?: AdapterConstants.NULL

    fun clearList() {
        items.clear()
        notifyDataSetChanged()
    }

    fun removeLoading() {
        val initPosition = getLastPosition()
        this.items.remove(loadingItem)
        notifyItemRemoved(initPosition)
    }

    fun setItem(rows: List<ViewType>) {
//        this.items.clear()
//        notifyItemRangeRemoved(0, getLastPosition())
        clearList()

        this.items.addAll(rows)
        if (isLoading) {
            items.add(loadingItem)
        }
        notifyItemRangeInserted(0, items.size)
    }

    fun setItem(row: ViewType) {
        clearList()
//        this.items.clear()
//        notifyItemRangeRemoved(0, getLastPosition())

        this.items.add(row)
        if (isLoading) {
            items.add(loadingItem)
        }
        notifyItemRangeInserted(0, items.size)
    }

    fun addItem(rows: ViewType) {
        // first remove loading and notify
        val initPosition = getLastPosition()
        if (isLoading && initPosition > 0) {
            this.items.removeAt(initPosition)
            notifyItemRemoved(initPosition)
        }

        // insert news and the loading at the end of the list
        this.items.add(rows)
        if (isLoading) {
            this.items.add(loadingItem)
        }
        notifyItemRangeChanged(initPosition, items.size + (if (isLoading) 1 else 0) /* plus loading rows */)
    }

    fun addItem(rows: List<ViewType>) {
        // first remove loading and notify
        val initPosition = getLastPosition()
        if (isLoading) {
            this.items.remove(loadingItem)
            notifyItemRemoved(initPosition)
        }

        // insert news and the loading at the end of the list
        this.items.addAll(rows)
        if (isLoading) {
            this.items.add(loadingItem)
        }
        notifyItemRangeChanged(initPosition, items.size + (if (isLoading) 1 else 0) /* plus loading rows */)
    }

    fun addLoading() {
        items.add(loadingItem)
        val initPosition = getLastPosition()
        notifyItemRemoved(initPosition)
    }

    private fun getLastPosition() = if (items.lastIndex == -1) 0 else items.lastIndex

}