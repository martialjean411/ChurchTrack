package com.churchtrack.app.ui.projects

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.data.database.entities.Project
import com.churchtrack.app.databinding.FragmentProjectDetailBinding
import com.churchtrack.app.viewmodel.ProjectViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class ProjectDetailFragment : Fragment() {

    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        ProjectViewModel.Factory(app.projectRepository)
    }

    private var projectId: Long = -1L
    private var currentProject: Project? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        projectId = arguments?.getLong("projectId", -1L) ?: -1L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (projectId <= 0) {
            findNavController().navigateUp()
            return
        }
        loadProject()
        setupContributions()
    }

    private fun loadProject() {
        lifecycleScope.launch {
            val project = viewModel.getProjectById(projectId) ?: return@launch
            currentProject = project
            bindProject(project)
        }
    }

    private fun bindProject(project: Project) {
        binding.tvProjectName.text = project.name
        binding.tvProjectDescription.text = project.description.ifEmpty { "Aucune description" }
        binding.tvCollected.text = "%.0f HTG".format(project.collectedAmount)
        binding.tvTarget.text = "/ %.0f HTG".format(project.targetAmount)
        binding.progressBar.progress = project.progressPercent()
        binding.tvProgress.text = "${project.progressPercent()}%"

        val statusLabel = when (project.status) {
            "COMPLETED" -> "✅ Terminé"
            "PAUSED" -> "⏸ En pause"
            else -> "🔄 En cours"
        }
        binding.tvStatus.text = statusLabel

        binding.btnAddContribution.setOnClickListener {
            showAddContributionDialog(project.id)
        }

        binding.btnEditProject.setOnClickListener {
            showEditDialog(project)
        }

        binding.btnDeleteProject.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Supprimer le projet")
                .setMessage("Supprimer \"${project.name}\" ?")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.deleteProject(project)
                    findNavController().navigateUp()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    private fun setupContributions() {
        val adapter = ContributionAdapter(
            onDeleteClick = { contribution ->
                lifecycleScope.launch {
                    viewModel.deleteContribution(contribution)
                    loadProject()
                }
            }
        )
        binding.rvContributions.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        viewModel.getContributionsForProject(projectId).observe(viewLifecycleOwner) { contributions ->
            adapter.submitList(contributions)
            binding.tvNoContributions.visibility =
                if (contributions.isEmpty()) View.VISIBLE else View.GONE
            binding.tvContributionCount.text = "${contributions.size} contribution(s)"
        }
    }

    private fun showAddContributionDialog(projectId: Long) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contribution, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ajouter une contribution")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val amount = dialogView.findViewById<TextInputEditText>(R.id.et_contribution_amount)
                    .text.toString().toDoubleOrNull() ?: 0.0
                val notes = dialogView.findViewById<TextInputEditText>(R.id.et_contribution_notes)
                    .text.toString()
                if (amount > 0) {
                    viewModel.addContribution(projectId, amount, com.churchtrack.app.util.DateUtil.today(), notes)
                    Snackbar.make(binding.root, "Contribution enregistrée", Snackbar.LENGTH_SHORT).show()
                    lifecycleScope.launch { loadProject() }
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditDialog(project: Project) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_project, null)
        dialogView.findViewById<TextInputEditText>(R.id.et_project_name).setText(project.name)
        dialogView.findViewById<TextInputEditText>(R.id.et_project_desc).setText(project.description)
        dialogView.findViewById<TextInputEditText>(R.id.et_project_target).setText(project.targetAmount.toLong().toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifier le projet")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.et_project_name).text.toString().trim()
                val desc = dialogView.findViewById<TextInputEditText>(R.id.et_project_desc).text.toString()
                val target = dialogView.findViewById<TextInputEditText>(R.id.et_project_target).text.toString()
                    .toDoubleOrNull() ?: project.targetAmount
                viewModel.updateProject(project.copy(name = name, description = desc, targetAmount = target))
                Snackbar.make(binding.root, "Projet mis à jour", Snackbar.LENGTH_SHORT).show()
                loadProject()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
