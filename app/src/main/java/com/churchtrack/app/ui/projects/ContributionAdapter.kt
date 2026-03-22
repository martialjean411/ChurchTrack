package com.churchtrack.app.ui.projects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.data.database.entities.ProjectContribution
import com.churchtrack.app.databinding.ItemContributionBinding
import com.churchtrack.app.util.DateUtil

class ContributionAdapter(
    private val onDeleteClick: (ProjectContribution) -> Unit
) : ListAdapter<ProjectContribution, ContributionAdapter.VH>(DIFF) {

    inner class VH(private val binding: ItemContributionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contribution: ProjectContribution) {
            binding.tvAmount.text = "%.0f HTG".format(contribution.amount)
            binding.tvDate.text = DateUtil.toDisplayFormat(contribution.date)
            binding.tvNotes.text = contribution.notes.ifEmpty { "—" }
            binding.btnDelete.setOnClickListener { onDeleteClick(contribution) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemContributionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ProjectContribution>() {
            override fun areItemsTheSame(a: ProjectContribution, b: ProjectContribution) = a.id == b.id
            override fun areContentsTheSame(a: ProjectContribution, b: ProjectContribution) = a == b
        }
    }
}
