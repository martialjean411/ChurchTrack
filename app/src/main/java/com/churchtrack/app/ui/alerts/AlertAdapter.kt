package com.churchtrack.app.ui.alerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.data.database.entities.AbsenceAlert
import com.churchtrack.app.databinding.ItemAlertBinding
import com.churchtrack.app.util.DateUtil

class AlertAdapter(
    private val onCallClick: (AbsenceAlert) -> Unit,
    private val onMessageClick: (AbsenceAlert) -> Unit,
    private val onFollowUpClick: (AbsenceAlert) -> Unit,
    private val onResolveClick: (AbsenceAlert) -> Unit,
    private val getMemberName: (Long) -> String
) : ListAdapter<AlertAdapter.AlertItem, AlertAdapter.AlertViewHolder>(DIFF_CALLBACK) {

    data class AlertItem(val alert: AbsenceAlert, val memberName: String)

    private val items = mutableListOf<AlertItem>()

    fun submitListWithNames(alerts: List<AbsenceAlert>, namesMap: Map<Long, String>) {
        val newItems = alerts.map { AlertItem(it, namesMap[it.memberId] ?: "Inconnu") }
        items.clear()
        items.addAll(newItems)
        submitList(newItems)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AlertViewHolder(private val binding: ItemAlertBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AlertItem) {
            val alert = item.alert
            binding.tvMemberName.text = item.memberName
            binding.tvAbsenceCount.text = "${alert.consecutiveAbsences} absences consécutives"
            binding.tvLastPresence.text = if (alert.lastAttendanceDate.isNotEmpty())
                "Dernière présence : ${DateUtil.toDisplayFormat(alert.lastAttendanceDate)}"
            else "Dernière présence : Inconnue"

            binding.tvStatus.text = when (alert.status) {
                "PENDING" -> "⚠️ En attente"
                "CONTACTED" -> "📞 Contacté"
                "RESOLVED" -> "✅ Résolu"
                else -> alert.status
            }

            binding.btnCall.setOnClickListener { onCallClick(alert) }
            binding.btnMessage.setOnClickListener { onMessageClick(alert) }
            binding.btnFollowUp.setOnClickListener { onFollowUpClick(alert) }
            binding.btnResolve.setOnClickListener { onResolveClick(alert) }

            binding.btnFollowUp.isEnabled = alert.status == "PENDING"
            binding.btnResolve.isEnabled = alert.status != "RESOLVED"
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AlertItem>() {
            override fun areItemsTheSame(a: AlertItem, b: AlertItem) = a.alert.id == b.alert.id
            override fun areContentsTheSame(a: AlertItem, b: AlertItem) = a == b
        }
    }
}
