package com.example.transactionmanagementsystem.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.transactionmanagementsystem.MainActivity
import com.example.transactionmanagementsystem.R
import com.example.transactionmanagementsystem.adapter.TransactionCardAdapter
import com.example.transactionmanagementsystem.databinding.FragmentTransactionListBinding
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import kotlinx.coroutines.launch

class TransactionListFragment : Fragment(R.layout.fragment_transaction_list), SearchView.OnQueryTextListener, MenuProvider {

    private var transactionListBinding: FragmentTransactionListBinding? = null
    private val binding get() = transactionListBinding!!

    private lateinit var transactionViewModel : TransactionViewModel
    private lateinit var transactionCardAdapter: TransactionCardAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        transactionListBinding = FragmentTransactionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        transactionViewModel = (activity as MainActivity).transactionViewModel
        setupTransactionListRecyclerView()

        binding.addTransactionFAB.setOnClickListener {
            it.findNavController().navigate(R.id.action_navbarFragment_to_addTransactionFragment2)
        }
    }

    private fun updateUI(transaction: List<Transaction>?) {
        if (transaction != null) {
            if (transaction.isNotEmpty()){
                binding.emptyTransactionImage.visibility = View.GONE
                binding.transactionListRecyclerView.visibility = View.VISIBLE
            } else {
                binding.emptyTransactionImage.visibility = View.VISIBLE
                binding.transactionListRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun setupTransactionListRecyclerView() {
        transactionCardAdapter = TransactionCardAdapter()
        binding.transactionListRecyclerView.apply{
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = transactionCardAdapter
        }

        lifecycleScope.launch {
            transactionViewModel.getTransactionsOrderedById().collect { transaction ->
                transactionCardAdapter.differ.submitList(transaction)
                updateUI(transaction)
            }
        }
    }

    private fun searchTransaction(query: String?){
        val searchQuery = "%$query"

        lifecycleScope.launch {
            transactionViewModel.searchTransaction(searchQuery).collect { list ->
                transactionCardAdapter.differ.submitList(list)
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchTransaction(newText)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        transactionListBinding = null
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.transaction_list_menu, menu)

        val menuSearch = menu.findItem(R.id.searchMenu).actionView as SearchView
        menuSearch.isSubmitButtonEnabled = false
        menuSearch.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return false
    }


}