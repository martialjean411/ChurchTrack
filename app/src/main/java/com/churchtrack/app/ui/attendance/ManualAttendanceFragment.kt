package com.churchtrack.app.ui.attendance

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.data.database.entities.Member
import com.churchtrack.app.databinding.FragmentManualAttendanceBinding
import com.churchtrack.app.viewmodel.AttendanceViewModel
import com.churchtrack.app.viewmodel.MemberViewModel
import com.google.android.material.snackbar.Snackbar

class ManualAttendanceFragment : Fragment() {

    private var _binding: FragmentManualAttendanceBinding? = null
    private val binding get() = _binding!!

    private val memberViewModel: MemberViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        MemberViewModel.Factory(app.memberRepository)
    }

    private val attendanceViewModel: AttendanceViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        AttendanceViewModel.Factory(app.attendanceRepository)
    }

    private lateinit var adapter: ManualAttendanceMemberAdapter
    private var serviceId: Long = -1L
    private val markedMembers = mutableSetOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        serviceId = arguments?.getLong("serviceId", -1L) ?: -1L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentManualAttendanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        loadExistingAttendances()
    }

    private fun setupRecyclerView() {
        adapter = ManualAttendanceMemberAdapter(
            markedMembers = markedMembers,
            onMarkClick = { member -> markMemberPresent(member) }
        )
        binding.rvMembers.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ManualAttendanceFragment.adapter
        }
    }

    private fun markMemberPresent(member: Member) {
        if (serviceId <= 0) return
        markedMembers.add(member.id)
        attendanceViewModel.markAttendance(member.id, serviceId, "MANUAL")
        adapter.notifyDataSetChanged()
        Snackbar.make(binding.root, "${member.fullName()} marqué présent", Snackbar.LENGTH_SHORT).show()
    }

    private fun observeData() {
        memberViewModel.searchResults.observe(viewLifecycleOwner) { members ->
            adapter.submitList(members)
        }
    }

    private fun loadExistingAttendances() {
        if (serviceId > 0) {
            attendanceViewModel.selectService(serviceId)
            attendanceViewModel.selectedServiceAttendances.observe(viewLifecycleOwner) { attendances ->
                attendances.forEach { markedMembers.add(it.memberId) }
                adapter.notifyDataSetChanged()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_members, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                memberViewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
