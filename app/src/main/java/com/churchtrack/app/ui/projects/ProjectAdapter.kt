package com.churchtrack.app.ui.projects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.R
import com.churchtrack.app.data.database.entities.Project
import com.churchtrack.app.databinding.ItemProjectBinding
import com.churchtrack.app.util.DateUtil

class ProjectAdapter(
    private val onProjectClick: (Project) -> Unit,
    private val onAddContributionClick: (Project) -> Unit,
    private val onEditClick: (Project) -> Unit,
    private val onDeleteClick: (Project) -> Unit
) : ListAdapter<Project, ProjectAdapter.ProjectViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProjectViewHolder(private val binding: ItemProjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(project: Project) {
            binding.tvProjectName.text = project.name
            binding.tvProjectDescription.text = project.description
            binding.tvCollected.text = "Collecté : ${DateUtil.formatCurrency(project.collectedAmount)}"
            binding.tvTarget.text = "Objectif : ${DateUtil.formatCurrency(project.targetAmount)}"
            binding.progressBar.progress = project.progressPercent()
            binding.tvProgress.text = "${project.progressPercent()}%"

            val statusColor = when (project.status) {
                "COMPLETED" -> binding.root.context.getColor(R.color.success)
                "PAUSED" -> binding.root.context.getColor(R.color.warning)
                else -> binding.root.context.getColor(R.color.gold)
            }
            binding.tvStatus.text = when (project.status) {
                "COMPLETED" -> "✅ Terminé"
                "PAUSED" -> "⏸️ En pause"
                else -> "🔄 En cours"
            }
            binding.tvStatus.setTextColor(statusColor)

            binding.root.setOnClickListener { onProjectClick(project) }
            binding.btnAddContribution.setOnClickListener { onAddContributionClick(project) }
            binding.btnEdit.setOnClickListener { onEditClick(project) }
            binding.btnDelete.setOnClickListener { onDeleteClick(project) }

            binding.btnAddContribution.isEnabled = project.status == "IN_PROGRESS"
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Project>() {
            override fun areItemsTheSame(a: Project, b: Project) = a.id == b.id
            override fun areContentsTheSame(a: Project, b: Project) = a == b
        }
    }
}
