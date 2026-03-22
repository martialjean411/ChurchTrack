package com.churchtrack.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.churchtrack.app.databinding.FragmentSettingsBinding
import com.churchtrack.app.ui.auth.LoginActivity
import com.churchtrack.app.util.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadCurrentSettings()
        setupListeners()
    }

    private fun loadCurrentSettings() {
        val threshold = SessionManager.getAbsenceThreshold(requireContext())
        binding.seekBarAbsence.progress = threshold - 2
        binding.tvAbsenceThreshold.text = "$threshold absences consécutives"

        binding.tvCurrentUser.text = SessionManager.getFullName(requireContext())
        binding.tvCurrentRole.text = if (SessionManager.isAdmin(requireContext())) "Administrateur" else "Utilisateur"
    }

    private fun setupListeners() {
        binding.seekBarAbsence.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val threshold = progress + 2
                binding.tvAbsenceThreshold.text = "$threshold absences consécutives"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val threshold = (seekBar?.progress ?: 0) + 2
                SessionManager.setAbsenceThreshold(requireContext(), threshold)
                Snackbar.make(binding.root, "Seuil d'alerte mis à jour : $threshold cultes", Snackbar.LENGTH_SHORT).show()
            }
        })

        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vous déconnecter ?")
                .setPositiveButton("Déconnexion") { _, _ ->
                    SessionManager.clearSession(requireContext())
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }

        binding.btnAbout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("À propos de ChurchTrack")
                .setMessage("ChurchTrack v1.0\n\nApplication de gestion d'église moderne.\n\nDéveloppée avec ❤️ pour les communautés chrétiennes.\n\n© 2024 ChurchTrack")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
