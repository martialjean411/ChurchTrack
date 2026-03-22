package com.churchtrack.app.ui.attendance

import android.os.Bundle
import android.view.*
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.FragmentAttendanceBinding
import com.churchtrack.app.service.AbsenceCheckWorker
import com.churchtrack.app.util.DateUtil
import com.churchtrack.app.util.SessionManager
import com.churchtrack.app.viewmodel.AttendanceViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AttendanceFragment : Fragment() {

    private var _binding: FragmentAttendanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttendanceViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        AttendanceViewModel.Factory(app.attendanceRepository)
    }

    private lateinit var serviceAdapter: ServiceAdapter
    private var selectedServiceId: Long = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupServiceList()
        observeData()
        setupButtons()
    }

    private fun setupServiceList() {
        serviceAdapter = ServiceAdapter(
            onServiceClick = { service ->
                selectedServiceId = service.id
                viewModel.selectService(service.id)
                updateSelectedServiceUI(service)
            }
        )
        binding.rvServices.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = serviceAdapter
        }
    }

    private fun updateSelectedServiceUI(service: com.churchtrack.app.data.database.entities.WorshipService) {
        binding.tvSelectedService.text = "${DateUtil.toDisplayFormat(service.date)} — ${formatServiceType(service.serviceType)}"
        binding.layoutAttendanceActions.visibility = View.VISIBLE
    }

    private fun formatServiceType(type: String): String = when (type) {
        "SUNDAY_MORNING" -> "Culte du Dimanche"
        "SUNDAY_EVENING" -> "Culte du Dimanche Soir"
        "WEDNESDAY" -> "Culte du Mercredi"
        "YOUTH" -> "Culte de Jeunesse"
        "SPECIAL" -> "Culte Spécial"
        else -> type
    }

    private fun observeData() {
        viewModel.allServices.observe(viewLifecycleOwner) { services ->
            serviceAdapter.submitList(services)
            binding.tvNoServices.visibility = if (services.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.presentCount.observe(viewLifecycleOwner) { count ->
            binding.tvPresentCount.text = "${count ?: 0} présents"
        }
    }

    private fun setupButtons() {
        binding.btnNewService.setOnClickListener {
            showNewServiceDialog()
        }

        binding.btnScanFingerprint.setOnClickListener {
            if (selectedServiceId <= 0) {
                Snackbar.make(binding.root, "Veuillez d'abord sélectionner un culte", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startFingerprintScan()
        }

        binding.btnManualAttendance.setOnClickListener {
            if (selectedServiceId <= 0) {
                Snackbar.make(binding.root, "Veuillez d'abord sélectionner un culte", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(
                R.id.action_attendance_to_manualAttendance,
                bundleOf("serviceId" to selectedServiceId)
            )
        }

        binding.btnViewAttendees.setOnClickListener {
            if (selectedServiceId > 0) {
                findNavController().navigate(
                    R.id.action_attendance_to_attendeeList,
                    bundleOf("serviceId" to selectedServiceId)
                )
            }
        }

        binding.btnCheckAbsences.setOnClickListener {
            checkAbsencesNow()
        }
    }

    private fun showNewServiceDialog() {
        val serviceTypes = arrayOf(
            "Culte du Dimanche", "Culte du Dimanche Soir",
            "Culte du Mercredi", "Culte de Jeunesse", "Culte Spécial"
        )
        val typeCodes = arrayOf("SUNDAY_MORNING", "SUNDAY_EVENING", "WEDNESDAY", "YOUTH", "SPECIAL")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nouveau culte")
            .setItems(serviceTypes) { _, which ->
                viewModel.createService(DateUtil.today(), typeCodes[which])
                Snackbar.make(binding.root, "Culte créé !", Snackbar.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun startFingerprintScan() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> showBiometricPrompt()
            else -> {
                Snackbar.make(
                    binding.root,
                    "Biométrie non disponible. Utilisez la saisie manuelle.",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // In a real implementation, we'd match the fingerprint to a member
                    // For now, show manual selection as fallback
                    Snackbar.make(
                        binding.root,
                        "Empreinte authentifiée ! Sélection du fidèle...",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(
                        R.id.action_attendance_to_manualAttendance,
                        bundleOf("serviceId" to selectedServiceId, "fromBiometric" to true)
                    )
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Snackbar.make(binding.root, "Erreur: $errString", Snackbar.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    Snackbar.make(binding.root, "Empreinte non reconnue", Snackbar.LENGTH_SHORT).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Pointer la présence")
            .setSubtitle("Scannez votre empreinte digitale")
            .setDescription("Placez votre doigt sur le capteur")
            .setNegativeButtonText("Annuler")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkAbsencesNow() {
        val threshold = SessionManager.getAbsenceThreshold(requireContext())
        CoroutineScope(Dispatchers.IO).launch {
            AbsenceCheckWorker.runCheck(requireContext(), threshold)
        }
        Snackbar.make(binding.root, "Vérification des absences en cours...", Snackbar.LENGTH_SHORT).show()
        findNavController().navigate(R.id.nav_alerts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
