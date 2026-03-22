package com.churchtrack.app.ui.alerts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.databinding.FragmentAlertsBinding
import com.churchtrack.app.viewmodel.AlertViewModel
import com.churchtrack.app.viewmodel.MemberViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AlertsFragment : Fragment() {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val alertViewModel: AlertViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        AlertViewModel.Factory(app.absenceAlertRepository)
    }

    private val memberViewModel: MemberViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        MemberViewModel.Factory(app.memberRepository)
    }

    private lateinit var adapter: AlertAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        setupTabs()
    }

    private fun setupRecyclerView() {
        adapter = AlertAdapter(
            onCallClick = { alert ->
                lifecycleScope.launch {
                    val member = memberViewModel.getMemberById(alert.memberId)
                    if (member != null && member.phone.isNotEmpty()) {
                        startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${member.phone}")))
                    } else {
                        Snackbar.make(binding.root, "Aucun numéro de téléphone", Snackbar.LENGTH_SHORT).show()
                    }
                }
            },
            onMessageClick = { alert ->
                lifecycleScope.launch {
                    val member = memberViewModel.getMemberById(alert.memberId)
                    if (member != null && member.phone.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${member.phone}"))
                        intent.putExtra("sms_body", "Bonjour ${member.firstName}, l'église pense à vous et prend de vos nouvelles.")
                        startActivity(intent)
                    }
                }
            },
            onFollowUpClick = { alert -> showFollowUpDialog(alert.id) },
            onResolveClick = { alert ->
                alertViewModel.resolveAlertsForMember(alert.memberId)
                Snackbar.make(binding.root, "Alerte résolue", Snackbar.LENGTH_SHORT).show()
            },
            getMemberName = { _ -> "" } // names resolved asynchronously via submitListWithNames
        )
        binding.rvAlerts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AlertsFragment.adapter
        }
    }

    private fun loadAlertsWithNames(alerts: List<com.churchtrack.app.data.database.entities.AbsenceAlert>) {
        lifecycleScope.launch {
            val membersMap = mutableMapOf<Long, String>()
            alerts.forEach { alert ->
                if (!membersMap.containsKey(alert.memberId)) {
                    val member = memberViewModel.getMemberById(alert.memberId)
                    membersMap[alert.memberId] = member?.fullName() ?: "Fidèle inconnu"
                }
            }
            adapter.submitListWithNames(alerts, membersMap)
            binding.tvEmptyAlerts.visibility = if (alerts.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun observeData() {
        alertViewModel.activeAlerts.observe(viewLifecycleOwner) { alerts ->
            loadAlertsWithNames(alerts)
        }

        alertViewModel.pendingAlertCount.observe(viewLifecycleOwner) { count ->
            binding.tvAlertCount.text = "${count ?: 0} alerte(s) en attente"
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> alertViewModel.activeAlerts.observe(viewLifecycleOwner) { loadAlertsWithNames(it) }
                    1 -> alertViewModel.allAlerts.observe(viewLifecycleOwner) { loadAlertsWithNames(it) }
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun showFollowUpDialog(alertId: Long) {
        val input = com.google.android.material.textfield.TextInputEditText(requireContext())
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Marquer comme suivi")
            .setMessage("Ajoutez une note de suivi (optionnel)")
            .setView(input)
            .setPositiveButton("Confirmer") { _, _ ->
                alertViewModel.markAsFollowedUp(alertId, input.text.toString())
                Snackbar.make(binding.root, "Suivi enregistré", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
