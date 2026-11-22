package com.example.someonetoldmeto.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.someonetoldmeto.R
import com.example.someonetoldmeto.databinding.ItemPersonBinding
import com.example.someonetoldmeto.model.PersonScore

class PeopleAdapter(private val onClick: (PersonScore) -> Unit) : ListAdapter<PersonScore, PeopleAdapter.Holder>(Diff) {

    class Holder(val b: ItemPersonBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val holder = Holder(ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        holder.b.root.setOnClickListener {
            holder.bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { onClick(getItem(it)) }
        }
        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val p = getItem(position)
        val b = holder.b
        val res = b.root.resources
        b.name.text = p.name
        b.score.text = if (p.tried == 0) res.getString(R.string.nothing_tried) else res.getString(R.string.score, p.liked, p.tried)
        b.score.setTextColor(b.root.context.getColor(if (p.tried == 0) R.color.text_muted else R.color.text))
        b.bar.isVisible = p.tried > 0
        b.bar.progress = if (p.tried == 0) 0 else p.liked * 100 / p.tried
        b.picks.text = res.getQuantityString(R.plurals.picks, p.total, p.total)
    }

    object Diff : DiffUtil.ItemCallback<PersonScore>() {
        override fun areItemsTheSame(a: PersonScore, b: PersonScore) = a.id == b.id
        override fun areContentsTheSame(a: PersonScore, b: PersonScore) = a == b
    }
}
