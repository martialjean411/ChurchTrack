package com.churchtrack.app.ui.users

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.FragmentUsersBinding
import com.churchtrack.app.data.repository.UserRepository
import com.churchtrack.app.data.database.entities.AppUser
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    private lateinit var userRepository: UserRepository
    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val app = requireActivity().application as ChurchTrackApp
        userRepository = app.userRepository

        setupRecyclerView()
        observeData()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = UserAdapter(
            onDeleteClick = { user ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Supprimer l'utilisateur")
                    .setMessage("Supprimer \"${user.fullName}\" ?")
                    .setPositiveButton("Supprimer") { _, _ ->
                        lifecycleScope.launch { userRepository.deleteUser(user) }
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            },
            onToggleRoleClick = { user ->
                val newRole = if (user.role == "ADMIN") "USER" else "ADMIN"
                lifecycleScope.launch {
                    userRepository.updateUser(user.copy(role = newRole))
                }
                Snackbar.make(binding.root, "Rôle mis à jour", Snackbar.LENGTH_SHORT).show()
            }
        )
        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@UsersFragment.adapter
        }
    }

    private fun observeData() {
        userRepository.allUsers.observe(viewLifecycleOwner) { users ->
            adapter.submitList(users)
            binding.tvUserCount.text = "${users.size} utilisateur(s)"
        }
    }

    private fun setupFab() {
        binding.fabAddUser.setOnClickListener {
            showAddUserDialog()
        }
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nouvel utilisateur")
            .setView(dialogView)
            .setPositiveButton("Créer") { _, _ ->
                val username = dialogView.findViewById<TextInputEditText>(R.id.et_username).text.toString().trim()
                val password = dialogView.findViewById<TextInputEditText>(R.id.et_password).text.toString()
                val fullName = dialogView.findViewById<TextInputEditText>(R.id.et_fullname).text.toString().trim()
                val roleSpinner = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.spinner_role)
                val role = if (roleSpinner.text.toString() == "Administrateur") "ADMIN" else "USER"

                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                    Snackbar.make(binding.root, "Remplissez tous les champs", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                lifecycleScope.launch {
                    try {
                        userRepository.createUser(username, password, fullName, role)
                        Snackbar.make(binding.root, "Utilisateur créé !", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Nom d'utilisateur déjà utilisé", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
