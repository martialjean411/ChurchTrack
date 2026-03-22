package com.churchtrack.app.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.databinding.FragmentDashboardBinding
import com.churchtrack.app.util.DateUtil
import com.churchtrack.app.util.SessionManager
import com.churchtrack.app.viewmodel.DashboardViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        DashboardViewModel.Factory(
            app.memberRepository,
            app.attendanceRepository,
            app.financialRepository,
            app.projectRepository,
            app.absenceAlertRepository
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvWelcome.text = "Bonjour, ${SessionManager.getFullName(requireContext()).split(" ").first()}"
        binding.tvDate.text = DateUtil.toFullDisplayFormat(DateUtil.today())

        observeData()
    }

    private fun observeData() {
        viewModel.totalMembers.observe(viewLifecycleOwner) { count ->
            binding.statTotalMembers.tvStatValue.text = count?.toString() ?: "0"
        }

        viewModel.todayPresentCount.observe(viewLifecycleOwner) { count ->
            binding.statTodayPresent.tvStatValue.text = count.toString()
        }

        viewModel.todayFinancial.observe(viewLifecycleOwner) { amount ->
            binding.statTodayFinance.tvStatValue.text = DateUtil.formatCurrency(amount ?: 0.0)
        }

        viewModel.activeProjectCount.observe(viewLifecycleOwner) { count ->
            binding.statActiveProjects.tvStatValue.text = count?.toString() ?: "0"
        }

        viewModel.pendingAlertCount.observe(viewLifecycleOwner) { count ->
            binding.statAlerts.tvStatValue.text = count?.toString() ?: "0"
            binding.statAlerts.root.alpha = if ((count ?: 0) > 0) 1f else 0.6f
        }

        viewModel.monthFinancial.observe(viewLifecycleOwner) { amount ->
            binding.tvMonthFinance.text = DateUtil.formatCurrency(amount ?: 0.0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
