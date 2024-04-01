package com.example.transactionmanagementsystem.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.transactionmanagementsystem.MainActivity
import com.example.transactionmanagementsystem.R
import com.example.transactionmanagementsystem.databinding.FragmentEditTransactionBinding
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditTransactionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditTransactionFragment : Fragment(R.layout.fragment_edit_transaction), MenuProvider {

    private var editTransactionBinding: FragmentEditTransactionBinding? = null
    private val binding get() = editTransactionBinding!!

    private lateinit var transactionViewModel: TransactionViewModel
    private lateinit var  currentTransaction: Transaction

    private val args: EditTransactionFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        editTransactionBinding = FragmentEditTransactionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        transactionViewModel = (activity as MainActivity).transactionViewModel

        currentTransaction = args.transaction!!


        binding.editTransactionTitle.setText(currentTransaction.title)
        binding.editTransactionCategory.setText(currentTransaction.category)
        binding.editTransactionAmount.setText(currentTransaction.amount.toString())
        binding.editTransactionDate.setText(currentTransaction.date.toString())
        binding.editTransactionAddress.setText(currentTransaction.address)
        binding.editTransactionLatitude.setText(currentTransaction.latitude.toString())
        binding.editTransactionLongitude.setText(currentTransaction.longitude.toString())


        binding.buttonSave.setOnClickListener {
            val transactionTitle = binding.editTransactionTitle.text.toString().trim()
            val transactionCategory = binding.editTransactionCategory.text.toString().trim()
            val transactionAmountStr = binding.editTransactionAmount.text.toString().trim()
            val address = binding.editTransactionAddress.text.toString().trim()
            var transactionAmount: Double? = null
            if (transactionTitle.isEmpty()) {
                Toast.makeText(context, "Title must be filled", Toast.LENGTH_SHORT).show()
            }
            if (transactionCategory.isEmpty()) {
                Toast.makeText(context, "Category must be filled", Toast.LENGTH_SHORT).show()
            }
            if (transactionAmountStr.isEmpty()) {
                Toast.makeText(context, "Category must be filled", Toast.LENGTH_SHORT).show()
            } else {
                transactionAmount = transactionAmountStr.toDouble()

            }

            if (transactionTitle.isNotEmpty() && transactionCategory.isNotEmpty() && transactionAmount != null) {
                val transaction = Transaction(currentTransaction.id, transactionTitle, transactionCategory, transactionAmount, currentTransaction.date, address, currentTransaction.latitude, currentTransaction.longitude)
                transactionViewModel.editTransaction(transaction)

                Toast.makeText(context, "Update saved", Toast.LENGTH_SHORT).show()
                view.findNavController().popBackStack(R.id.transactionListFragment, false)
            }
        }



    }

    private fun deleteTransaction() {
        AlertDialog.Builder(activity).apply{
            setTitle("Delete Transaction")
            setMessage("Are you sure want to delete this transaction?")
            setPositiveButton("Delete"){_,_ ->
                transactionViewModel.deleteTransaction(currentTransaction)
                Toast.makeText(context, "Transaction Deleted", Toast.LENGTH_SHORT).show()
                view?.findNavController()?.popBackStack(R.id.transactionListFragment, false)
            }
            setNegativeButton("Cancel", null)
        }.create().show()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.edit_transaction_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return when(menuItem.itemId){
            R.id.deleteMenu -> {
                deleteTransaction()
                true
            } else -> false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        editTransactionBinding = null
    }

}