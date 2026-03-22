package com.churchtrack.app.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.data.database.entities.WorshipService
import com.churchtrack.app.databinding.ItemServiceBinding
import com.churchtrack.app.util.DateUtil

class ServiceAdapter(
    private val onServiceClick: (WorshipService) -> Unit
) : ListAdapter<WorshipService, ServiceAdapter.ServiceViewHolder>(DIFF_CALLBACK) {

    private var selectedId: Long = -1L

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val binding = ItemServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ServiceViewHolder(private val binding: ItemServiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(service: WorshipService) {
            binding.tvServiceDate.text = DateUtil.toDisplayFormat(service.date)
            binding.tvServiceType.text = formatType(service.serviceType)
            binding.root.isSelected = service.id == selectedId
            binding.root.setOnClickListener {
                val prevSelected = selectedId
                selectedId = service.id
                notifyItemChanged(currentList.indexOfFirst { it.id == prevSelected })
                notifyItemChanged(adapterPosition)
                onServiceClick(service)
            }
        }

        private fun formatType(type: String): String = when (type) {
            "SUNDAY_MORNING" -> "Dimanche"
            "SUNDAY_EVENING" -> "Dim. Soir"
            "WEDNESDAY" -> "Mercredi"
            "YOUTH" -> "Jeunesse"
            "SPECIAL" -> "Spécial"
            else -> type
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WorshipService>() {
            override fun areItemsTheSame(a: WorshipService, b: WorshipService) = a.id == b.id
            override fun areContentsTheSame(a: WorshipService, b: WorshipService) = a == b
        }
    }
}
