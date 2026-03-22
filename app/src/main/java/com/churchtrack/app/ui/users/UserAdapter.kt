package com.churchtrack.app.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.churchtrack.app.data.database.entities.AppUser
import com.churchtrack.app.databinding.ItemUserBinding
import com.churchtrack.app.util.DateUtil

class UserAdapter(
    private val onDeleteClick: (AppUser) -> Unit,
    private val onToggleRoleClick: (AppUser) -> Unit
) : ListAdapter<AppUser, UserAdapter.UserViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: AppUser) {
            binding.tvUserName.text = user.fullName
            binding.tvUsername.text = "@${user.username}"
            binding.tvRole.text = if (user.role == "ADMIN") "Administrateur" else "Utilisateur"
            binding.tvLastLogin.text = if (user.lastLogin > 0)
                "Dernière connexion: ${DateUtil.formatTimestamp(user.lastLogin)}"
            else "Jamais connecté"

            binding.btnToggleRole.text = if (user.role == "ADMIN") "Rétrograder" else "Promouvoir"
            binding.btnToggleRole.setOnClickListener { onToggleRoleClick(user) }
            binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<AppUser>() {
            override fun areItemsTheSame(a: AppUser, b: AppUser) = a.id == b.id
            override fun areContentsTheSame(a: AppUser, b: AppUser) = a == b
        }
    }
}
