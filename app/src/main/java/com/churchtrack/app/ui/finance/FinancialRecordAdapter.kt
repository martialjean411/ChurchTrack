package com.churchtrack.app.ui.finance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.data.database.entities.FinancialRecord
import com.churchtrack.app.databinding.ItemFinancialRecordBinding
import com.churchtrack.app.util.DateUtil

class FinancialRecordAdapter(
    private val onDeleteClick: (FinancialRecord) -> Unit
) : ListAdapter<FinancialRecord, FinancialRecordAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFinancialRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemFinancialRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: FinancialRecord) {
            binding.tvDate.text = DateUtil.toDisplayFormat(record.date)
            binding.tvOffering.text = "Offrandes: ${DateUtil.formatCurrency(record.offeringAmount)}"
            binding.tvTithe.text = "Dîmes: ${DateUtil.formatCurrency(record.titheAmount)}"
            if (record.specialOfferingAmount > 0) {
                binding.tvSpecial.text = "Spécial: ${DateUtil.formatCurrency(record.specialOfferingAmount)}"
                binding.tvSpecial.visibility = android.view.View.VISIBLE
            } else {
                binding.tvSpecial.visibility = android.view.View.GONE
            }
            binding.tvTotal.text = "Total: ${DateUtil.formatCurrency(record.totalAmount())}"
            binding.tvNotes.text = record.notes
            binding.tvNotes.visibility = if (record.notes.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.btnDelete.setOnClickListener { onDeleteClick(record) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FinancialRecord>() {
            override fun areItemsTheSame(a: FinancialRecord, b: FinancialRecord) = a.id == b.id
            override fun areContentsTheSame(a: FinancialRecord, b: FinancialRecord) = a == b
        }
    }
}
