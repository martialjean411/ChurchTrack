package com.churchtrack.app.ui.attendance

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.R
import com.churchtrack.app.data.database.entities.Member
import com.churchtrack.app.databinding.ItemAttendanceMemberBinding

class ManualAttendanceMemberAdapter(
    private val markedMembers: Set<Long>,
    private val onMarkClick: (Member) -> Unit
) : ListAdapter<Member, ManualAttendanceMemberAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAttendanceMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAttendanceMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(member: Member) {
            binding.tvMemberName.text = member.fullName()
            binding.tvMemberContact.text = member.phone
            val isMarked = markedMembers.contains(member.id)
            binding.btnMark.isEnabled = !isMarked
            binding.btnMark.text = if (isMarked) "Présent ✓" else "Marquer présent"
            binding.btnMark.setBackgroundColor(
                if (isMarked)
                    binding.root.context.getColor(R.color.success)
                else
                    binding.root.context.getColor(R.color.gold)
            )
            binding.btnMark.setOnClickListener {
                if (!isMarked) onMarkClick(member)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Member>() {
            override fun areItemsTheSame(a: Member, b: Member) = a.id == b.id
            override fun areContentsTheSame(a: Member, b: Member) = a == b
        }
    }
}
