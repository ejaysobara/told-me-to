package com.example.someonetoldmeto.ui

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.databinding.ItemRecBinding
import com.example.someonetoldmeto.model.Recommendation

class RecAdapter(private val onClick: (Recommendation) -> Unit) : ListAdapter<Recommendation, RecAdapter.Holder>(Diff) {

    class Holder(val b: ItemRecBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(ItemRecBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        holder.b.root.setOnClickListener {
            holder.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { onClick(getItem(it)) }
        }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val rec = getItem(position)
        val b = holder.b
        val when_ = DateUtils.getRelativeTimeSpanString(rec.createdAt, System.currentTimeMillis(), DateUtils.DAY_IN_MILLIS)
        b.title.text = rec.title
        b.type.setText(rec.type.label())
        b.meta.text = b.root.context.getString(R.string.row_meta, rec.person, when_)
        b.status.setText(rec.status.label())
    }

    object Diff : DiffUtil.ItemCallback<Recommendation>() {
        override fun areItemsTheSame(a: Recommendation, b: Recommendation) = a.id == b.id
        override fun areContentsTheSame(a: Recommendation, b: Recommendation) = a == b
    }
}
