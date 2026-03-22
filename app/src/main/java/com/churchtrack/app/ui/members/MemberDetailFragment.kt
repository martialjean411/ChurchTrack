package com.churchtrack.app.ui.members

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.FragmentMemberDetailBinding
import com.churchtrack.app.util.DateUtil
import com.churchtrack.app.viewmodel.AttendanceViewModel
import com.churchtrack.app.viewmodel.MemberViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File

class MemberDetailFragment : Fragment() {

    private var _binding: FragmentMemberDetailBinding? = null
    private val binding get() = _binding!!

    private val memberViewModel: MemberViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        MemberViewModel.Factory(app.memberRepository)
    }

    private val attendanceViewModel: AttendanceViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        AttendanceViewModel.Factory(app.attendanceRepository)
    }

    private var memberId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        memberId = arguments?.getLong("memberId", -1L) ?: -1L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMemberDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (memberId <= 0) {
            findNavController().navigateUp()
            return
        }
        loadMember()
    }

    private fun loadMember() {
        lifecycleScope.launch {
            val member = memberViewModel.getMemberById(memberId) ?: return@launch

            binding.tvFullName.text = member.fullName()
            binding.tvPhone.text = member.phone.ifEmpty { "Non renseigné" }
            binding.tvEmail.text = member.email.ifEmpty { "Non renseigné" }
            binding.tvGender.text = if (member.gender == "M") "Homme" else "Femme"
            binding.tvBirthDate.text = if (member.birthDate.isNotEmpty())
                DateUtil.toDisplayFormat(member.birthDate) else "Non renseignée"
            binding.tvAddress.text = member.address.ifEmpty { "Non renseignée" }
            binding.tvNotes.text = member.notes.ifEmpty { "Aucune" }
            binding.tvFingerprintStatus.text = if (member.hasFingerprintRegistered)
                "✅ Empreinte enregistrée" else "❌ Aucune empreinte"
            binding.tvMemberSince.text = if (member.memberSince.isNotEmpty())
                "Membre depuis : ${DateUtil.toDisplayFormat(member.memberSince)}"
            else "Date d'adhésion inconnue"

            // Photo
            if (member.photoPath.isNotEmpty() && File(member.photoPath).exists()) {
                binding.ivPhoto.load(File(member.photoPath)) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                }
            } else {
                binding.tvInitials.text = "${member.firstName.firstOrNull() ?: ""}${member.lastName.firstOrNull() ?: ""}"
                binding.tvInitials.visibility = View.VISIBLE
            }

            // Attendance history
            attendanceViewModel.getAttendancesForMember(memberId)
                .observe(viewLifecycleOwner) { attendances ->
                    binding.tvAttendanceCount.text = "${attendances.size} culte(s) assisté(s)"
                }

            // Button actions
            binding.btnEdit.setOnClickListener {
                findNavController().navigate(
                    R.id.action_memberDetail_to_addEditMember,
                    bundleOf("memberId" to memberId)
                )
            }

            binding.btnCall.setOnClickListener {
                if (member.phone.isNotEmpty()) {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}")))
                } else {
                    Snackbar.make(binding.root, "Aucun numéro de téléphone", Snackbar.LENGTH_SHORT).show()
                }
            }

            binding.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Désactiver le fidèle")
                    .setMessage("Désactiver ${member.fullName()} ? Il n'apparaîtra plus dans les listes actives.")
                    .setPositiveButton("Désactiver") { _, _ ->
                        memberViewModel.deleteMember(member)
                        findNavController().navigateUp()
                    }
                    .setNegativeButton("Annuler", null)
                    .show()
            }
        }
    }

    private fun AttendanceViewModel.getAttendancesForMember(memberId: Long) =
        (requireActivity().application as ChurchTrackApp)
            .attendanceRepository.getAttendancesForMember(memberId)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
