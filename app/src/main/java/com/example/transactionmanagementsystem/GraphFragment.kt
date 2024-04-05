package com.example.transactionmanagementsystem

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.example.transactionmanagementsystem.databinding.FragmentAddTransactionBinding
import com.example.transactionmanagementsystem.databinding.FragmentGraphBinding
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class GraphFragment : Fragment(R.layout.fragment_graph) {
    private var chart: AnyChartView? = null
    private var graphBinding: FragmentGraphBinding? = null
    private val binding get() = graphBinding!!
    private lateinit var transactionViewModel: TransactionViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        graphBinding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        transactionViewModel = (activity as MainActivity).transactionViewModel
        chart = binding.pieChart

        configChartView()
        activity?.title = "Graph"
    }

    private fun configChartView() {
        lifecycleScope.launch {
            transactionViewModel.getExpenseTotalByUserId().collect { expense ->
                // Inside this block, 'expense' holds the emitted value from the flow
                val income = transactionViewModel.getIncomeTotalByUserId().firstOrNull() ?: 0.0

                val pie: Pie = AnyChart.pie()

                val dataPieChart: MutableList<DataEntry> = mutableListOf()

                dataPieChart.add(ValueDataEntry("Expense", expense))
                dataPieChart.add(ValueDataEntry("Income", income))

                pie.data(dataPieChart)
                pie.title("Transaction Overview")
                chart!!.setChart(pie)
            }
        }
    }
}