package com.churchtrack.app.ui.members

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.churchtrack.app.R
import com.churchtrack.app.data.database.entities.Member
import com.churchtrack.app.databinding.ItemMemberBinding
import java.io.File

class MemberAdapter(
    private val onItemClick: (Member) -> Unit,
    private val onEditClick: (Member) -> Unit
) : ListAdapter<Member, MemberAdapter.MemberViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(private val binding: ItemMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(member: Member) {
            binding.tvMemberName.text = member.fullName()
            binding.tvMemberContact.text = member.phone
            binding.tvMemberGender.text = if (member.gender == "M") "Homme" else "Femme"
            binding.ivFingerprint.visibility = if (member.hasFingerprintRegistered)
                android.view.View.VISIBLE else android.view.View.GONE

            // Load photo
            if (member.photoPath.isNotEmpty() && File(member.photoPath).exists()) {
                binding.ivMemberPhoto.load(File(member.photoPath)) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person)
                }
            } else {
                binding.ivMemberPhoto.setImageResource(R.drawable.ic_person)
                // Show initials
                binding.tvInitials.text = "${member.firstName.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}"
                binding.tvInitials.visibility = android.view.View.VISIBLE
            }

            binding.root.setOnClickListener { onItemClick(member) }
            binding.btnEdit.setOnClickListener { onEditClick(member) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Member>() {
            override fun areItemsTheSame(oldItem: Member, newItem: Member) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Member, newItem: Member) = oldItem == newItem
        }
    }
}
