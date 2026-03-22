package com.churchtrack.app.ui.members

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.FragmentMembersBinding
import com.churchtrack.app.viewmodel.MemberViewModel

class MembersFragment : Fragment() {

    private var _binding: FragmentMembersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MemberViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        MemberViewModel.Factory(app.memberRepository)
    }

    private lateinit var adapter: MemberAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()

        binding.fabAddMember.setOnClickListener {
            findNavController().navigate(R.id.action_members_to_addEditMember)
        }
    }

    private fun setupRecyclerView() {
        adapter = MemberAdapter(
            onItemClick = { member ->
                findNavController().navigate(
                    R.id.action_members_to_memberDetail,
                    bundleOf("memberId" to member.id)
                )
            },
            onEditClick = { member ->
                findNavController().navigate(
                    R.id.action_members_to_addEditMember,
                    bundleOf("memberId" to member.id)
                )
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@MembersFragment.adapter
        }
    }

    private fun observeData() {
        viewModel.searchResults.observe(viewLifecycleOwner) { members ->
            adapter.submitList(members)
            binding.tvEmptyState.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.activeMemberCount.observe(viewLifecycleOwner) { count ->
            binding.tvMemberCount.text = "${count ?: 0} membres"
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
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
