package com.gurumi.inapptest

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.droidcba.kedditbysteps.commons.recyclerView.BaseRecyclerViewAdapter
import com.droidcba.kedditbysteps.commons.recyclerView.adapter.ViewType
import com.droidcba.kedditbysteps.commons.recyclerView.adapter.ViewTypeDelegateAdapter
import com.gurumi.inapptest.recyclerView.adapter.AdapterConstants
import com.gurumi.inapptest.recyclerView.holder.AndroidExtensionsViewHolder
import kotlinx.android.synthetic.main.row_sponsor.view.*

/**
 *
 * Create : Kwon IkYoung
 * Date : 2018. 1. 29.
 */

class DevSponsorAdapter(block: (SponsorVo) -> Unit) : BaseRecyclerViewAdapter(false) {

    init {
        delegateAdapters.put(AdapterConstants.ROW_SPONSOR, SponsorDelegateAdapter(block))
    }
}

class SponsorDelegateAdapter(val block: (SponsorVo) -> Unit) : ViewTypeDelegateAdapter {

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = SponsorViewHolder(parent)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        holder as SponsorViewHolder
        holder.bind(item as SponsorVo)
    }

    inner class SponsorViewHolder(parent: ViewGroup) : AndroidExtensionsViewHolder(
            parent.inflate(R.layout.row_sponsor)) {

        fun bind(item: SponsorVo) {
            item.apply {
                itemView.title.text = this.title.split("(")[0]
                itemView.subTitle.text = this.description
                itemView.sponsorPrice.text = this.price
                itemView.setOnClickListener {
                    block(this)
                }
            }
        }
    }
}