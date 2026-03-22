package com.churchtrack.app.ui.projects

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.FragmentProjectsBinding
import com.churchtrack.app.viewmodel.ProjectViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class ProjectsFragment : Fragment() {

    private var _binding: FragmentProjectsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        ProjectViewModel.Factory(app.projectRepository)
    }

    private lateinit var adapter: ProjectAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProjectsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = ProjectAdapter(
            onProjectClick = { project ->
                findNavController().navigate(
                    R.id.action_projects_to_projectDetail,
                    bundleOf("projectId" to project.id)
                )
            },
            onAddContributionClick = { project ->
                showAddContributionDialog(project.id)
            },
            onEditClick = { project ->
                showEditProjectDialog(project)
            },
            onDeleteClick = { project ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Supprimer le projet")
                    .setMessage("Supprimer \"${project.name}\" ? Cette action est irréversible.")
                    .setPositiveButton("Supprimer") { _, _ -> viewModel.deleteProject(project) }
                    .setNegativeButton("Annuler", null)
                    .show()
            }
        )
        binding.rvProjects.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ProjectsFragment.adapter
        }
    }

    private fun observeData() {
        viewModel.allProjects.observe(viewLifecycleOwner) { projects ->
            adapter.submitList(projects)
            binding.tvEmptyState.visibility = if (projects.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.activeProjectCount.observe(viewLifecycleOwner) { count ->
            binding.tvProjectCount.text = "${count ?: 0} projet(s) actif(s)"
        }
    }

    private fun setupFab() {
        binding.fabAddProject.setOnClickListener {
            showCreateProjectDialog()
        }
    }

    private fun showCreateProjectDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_project, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nouveau projet")
            .setView(dialogView)
            .setPositiveButton("Créer") { _, _ ->
                val etName = dialogView.findViewById<TextInputEditText>(R.id.et_project_name)
                val etDesc = dialogView.findViewById<TextInputEditText>(R.id.et_project_desc)
                val etTarget = dialogView.findViewById<TextInputEditText>(R.id.et_project_target)
                val name = etName.text.toString().trim()
                val target = etTarget.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isEmpty() || target <= 0) {
                    Snackbar.make(binding.root, "Remplissez le nom et l'objectif", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.createProject(name, etDesc.text.toString(), target)
                Snackbar.make(binding.root, "Projet créé !", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showAddContributionDialog(projectId: Long) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contribution, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ajouter une contribution")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val etAmount = dialogView.findViewById<TextInputEditText>(R.id.et_contribution_amount)
                val etNotes = dialogView.findViewById<TextInputEditText>(R.id.et_contribution_notes)
                val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
                if (amount > 0) {
                    viewModel.addContribution(
                        projectId, amount,
                        com.churchtrack.app.util.DateUtil.today(),
                        etNotes.text.toString()
                    )
                    Snackbar.make(binding.root, "Contribution enregistrée", Snackbar.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showEditProjectDialog(project: com.churchtrack.app.data.database.entities.Project) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_project, null)
        dialogView.findViewById<TextInputEditText>(R.id.et_project_name).setText(project.name)
        dialogView.findViewById<TextInputEditText>(R.id.et_project_desc).setText(project.description)
        dialogView.findViewById<TextInputEditText>(R.id.et_project_target).setText(project.targetAmount.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Modifier le projet")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val name = dialogView.findViewById<TextInputEditText>(R.id.et_project_name).text.toString().trim()
                val desc = dialogView.findViewById<TextInputEditText>(R.id.et_project_desc).text.toString()
                val target = dialogView.findViewById<TextInputEditText>(R.id.et_project_target).text.toString().toDoubleOrNull() ?: project.targetAmount
                viewModel.updateProject(project.copy(name = name, description = desc, targetAmount = target))
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
