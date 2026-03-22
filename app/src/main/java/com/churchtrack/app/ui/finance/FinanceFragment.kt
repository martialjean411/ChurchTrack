package com.churchtrack.app.ui.finance

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.churchtrack.app.ChurchTrackApp
import com.churchtrack.app.R
import com.churchtrack.app.databinding.FragmentFinanceBinding
import com.churchtrack.app.util.DateUtil
import com.churchtrack.app.util.SessionManager
import com.churchtrack.app.viewmodel.FinancialViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

class FinanceFragment : Fragment() {

    private var _binding: FragmentFinanceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FinancialViewModel by viewModels {
        val app = requireActivity().application as ChurchTrackApp
        FinancialViewModel.Factory(app.financialRepository)
    }

    private lateinit var adapter: FinancialRecordAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFinanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupChart()
        setupFab()
    }

    private fun setupRecyclerView() {
        adapter = FinancialRecordAdapter(
            onDeleteClick = { record ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Supprimer")
                    .setMessage("Supprimer cet enregistrement ?")
                    .setPositiveButton("Supprimer") { _, _ -> viewModel.deleteRecord(record) }
                    .setNegativeButton("Annuler", null)
                    .show()
            }
        )
        binding.rvRecords.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FinanceFragment.adapter
        }
    }

    private fun observeData() {
        viewModel.todayTotal.observe(viewLifecycleOwner) { total ->
            binding.tvTodayTotal.text = DateUtil.formatCurrency(total ?: 0.0)
        }

        viewModel.monthTotal.observe(viewLifecycleOwner) { total ->
            binding.tvMonthTotal.text = DateUtil.formatCurrency(total ?: 0.0)
        }

        viewModel.yearTotal.observe(viewLifecycleOwner) { total ->
            binding.tvYearTotal.text = DateUtil.formatCurrency(total ?: 0.0)
        }

        viewModel.periodRecords.observe(viewLifecycleOwner) { records ->
            adapter.submitList(records)
            binding.tvEmptyState.visibility = if (records.isEmpty()) View.VISIBLE else View.GONE
            updateChart(records)
        }
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = true
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            axisRight.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
        }
    }

    private fun updateChart(records: List<com.churchtrack.app.data.database.entities.FinancialRecord>) {
        if (records.isEmpty()) return

        val offeringEntries = mutableListOf<BarEntry>()
        val titheEntries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        records.takeLast(7).forEachIndexed { index, record ->
            offeringEntries.add(BarEntry(index.toFloat(), record.offeringAmount.toFloat()))
            titheEntries.add(BarEntry(index.toFloat(), record.titheAmount.toFloat()))
            labels.add(DateUtil.toDisplayFormat(record.date).takeLast(5))
        }

        val offeringSet = BarDataSet(offeringEntries, "Offrandes").apply {
            color = resources.getColor(R.color.gold, null)
        }
        val titheSet = BarDataSet(titheEntries, "Dîmes").apply {
            color = resources.getColor(R.color.blue_primary, null)
        }

        val barData = BarData(offeringSet, titheSet).apply {
            barWidth = 0.3f
        }

        binding.barChart.apply {
            data = barData
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            groupBars(0f, 0.2f, 0.05f)
            invalidate()
        }
    }

    private fun setupFab() {
        binding.fabAddRecord.setOnClickListener {
            showAddRecordDialog()
        }
    }

    private fun showAddRecordDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_financial_record, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enregistrer des fonds")
            .setView(dialogView)
            .setPositiveButton("Enregistrer") { _, _ ->
                val etOffering = dialogView.findViewById<TextInputEditText>(R.id.et_offering)
                val etTithe = dialogView.findViewById<TextInputEditText>(R.id.et_tithe)
                val etSpecial = dialogView.findViewById<TextInputEditText>(R.id.et_special)
                val etNotes = dialogView.findViewById<TextInputEditText>(R.id.et_notes)

                val offering = etOffering.text.toString().toDoubleOrNull() ?: 0.0
                val tithe = etTithe.text.toString().toDoubleOrNull() ?: 0.0
                val special = etSpecial.text.toString().toDoubleOrNull() ?: 0.0

                viewModel.addRecord(
                    date = DateUtil.today(),
                    offeringAmount = offering,
                    titheAmount = tithe,
                    specialAmount = special,
                    notes = etNotes.text.toString(),
                    recordedBy = SessionManager.getUserId(requireContext())
                )
                Snackbar.make(binding.root, "Enregistrement sauvegardé", Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
