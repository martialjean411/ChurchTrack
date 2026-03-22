package com.churchtrack.app.ui.users

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.data.repository.UserRepository
import com.churchtrack.app.databinding.FragmentUsersBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
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
        userRepository = (requireActivity().application as ChurchTrackApp).userRepository
        setupRecyclerView()
        observeData()
        binding.fabAddUser.setOnClickListener { showAddUserDialog() }
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
                lifecycleScope.launch { userRepository.updateUser(user.copy(role = newRole)) }
                val label = if (newRole == "ADMIN") "Administrateur" else "Utilisateur"
                Snackbar.make(binding.root, "Rôle mis à jour : $label", Snackbar.LENGTH_SHORT).show()
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

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_user, null)
        val roleSpinner = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinner_role)
        val roles = arrayOf("Utilisateur", "Administrateur")
        roleSpinner.setAdapter(
            android.widget.ArrayAdapter(requireContext(), R.layout.item_dropdown, roles)
        )
        roleSpinner.setText("Utilisateur", false)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nouvel utilisateur")
            .setView(dialogView)
            .setPositiveButton("Créer") { _, _ ->
                val username = dialogView.findViewById<TextInputEditText>(R.id.et_username).text.toString().trim()
                val password = dialogView.findViewById<TextInputEditText>(R.id.et_password).text.toString()
                val fullName = dialogView.findViewById<TextInputEditText>(R.id.et_fullname).text.toString().trim()
                val role = if (roleSpinner.text.toString() == "Administrateur") "ADMIN" else "USER"

                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
                    Snackbar.make(binding.root, "Remplissez tous les champs obligatoires", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                lifecycleScope.launch {
                    try {
                        userRepository.createUser(username, password, fullName, role)
                        Snackbar.make(binding.root, "Utilisateur \"$fullName\" créé !", Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Snackbar.make(binding.root, "Identifiant \"$username\" déjà utilisé", Snackbar.LENGTH_LONG).show()
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
