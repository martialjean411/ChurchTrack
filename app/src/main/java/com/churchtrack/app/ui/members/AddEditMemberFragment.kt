package com.churchtrack.app.ui.members

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.data.database.entities.Member
import com.churchtrack.app.databinding.FragmentAddEditMemberBinding
import com.churchtrack.app.util.DateUtil
import com.churchtrack.app.viewmodel.MemberViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class AddEditMemberFragment : Fragment() {

    private var _binding: FragmentAddEditMemberBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemberViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        MemberViewModel.Factory(app.memberRepository)
    }

    private var memberId: Long = -1L
    private var existingMember: Member? = null
    private var selectedPhotoUri: Uri? = null
    private var selectedPhotoPath: String = ""

    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPhotoUri = it
            binding.ivMemberPhoto.load(it) {
                transformations(CircleCropTransformation())
            }
            savePhotoLocally(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddEditMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        memberId = arguments?.getLong("memberId", -1L) ?: -1L

        setupGenderDropdown()
        setupDatePicker()
        setupPhotoButton()
        setupSaveButton()

        if (memberId != -1L) {
            loadMemberData()
        }
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Homme", "Femme")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_dropdown, genders)
        binding.spinnerGender.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etBirthDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = "%04d-%02d-%02d".format(year, month + 1, day)
                    binding.etBirthDate.setText(DateUtil.toDisplayFormat(date))
                },
                cal.get(Calendar.YEAR) - 20,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupPhotoButton() {
        binding.btnSelectPhoto.setOnClickListener {
            photoPickerLauncher.launch("image/*")
        }
    }

    private fun savePhotoLocally(uri: Uri) {
        lifecycleScope.launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return@launch
                val photoDir = File(requireContext().filesDir, "member_photos")
                photoDir.mkdirs()
                val photoFile = File(photoDir, "member_${System.currentTimeMillis()}.jpg")
                FileOutputStream(photoFile).use { out ->
                    inputStream.copyTo(out)
                }
                selectedPhotoPath = photoFile.absolutePath
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun loadMemberData() {
        lifecycleScope.launch {
            val member = viewModel.getMemberById(memberId)
            member?.let {
                existingMember = it
                binding.etFirstName.setText(it.firstName)
                binding.etLastName.setText(it.lastName)
                binding.etPhone.setText(it.phone)
                binding.etEmail.setText(it.email)
                binding.spinnerGender.setText(if (it.gender == "M") "Homme" else "Femme", false)
                binding.etBirthDate.setText(DateUtil.toDisplayFormat(it.birthDate))
                binding.etAddress.setText(it.address)
                binding.etNotes.setText(it.notes)
                selectedPhotoPath = it.photoPath

                if (it.photoPath.isNotEmpty() && File(it.photoPath).exists()) {
                    binding.ivMemberPhoto.load(File(it.photoPath)) {
                        transformations(CircleCropTransformation())
                    }
                }
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val genderText = binding.spinnerGender.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || genderText.isEmpty()) {
                Snackbar.make(binding.root, "Veuillez remplir les champs obligatoires", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = if (genderText == "Homme") "M" else "F"
            val birthDateDisplay = binding.etBirthDate.text.toString()
            val birthDate = if (birthDateDisplay.isNotEmpty()) DateUtil.fromDisplayFormat(birthDateDisplay) else ""

            val member = (existingMember ?: Member(
                firstName = "", lastName = "", phone = "", gender = "M"
            )).copy(
                firstName = firstName,
                lastName = lastName,
                phone = phone,
                email = binding.etEmail.text.toString().trim(),
                gender = gender,
                birthDate = birthDate,
                address = binding.etAddress.text.toString().trim(),
                notes = binding.etNotes.text.toString().trim(),
                photoPath = selectedPhotoPath.ifEmpty { existingMember?.photoPath ?: "" },
                hasFingerprintRegistered = existingMember?.hasFingerprintRegistered ?: false
            )

            if (memberId != -1L) {
                viewModel.updateMember(member)
            } else {
                viewModel.insertMember(member)
            }

            viewModel.operationResult.observe(viewLifecycleOwner) { result ->
                if (result.isSuccess) {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
