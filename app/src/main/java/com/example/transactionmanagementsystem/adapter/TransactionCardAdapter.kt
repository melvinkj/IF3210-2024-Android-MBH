package com.example.transactionmanagementsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.transactionmanagementsystem.R
import com.example.transactionmanagementsystem.databinding.CardViewTransactionBinding
import com.example.transactionmanagementsystem.fragments.NavbarFragmentDirections
import com.example.transactionmanagementsystem.fragments.TransactionListFragmentDirections
import com.example.transactionmanagementsystem.models.Transaction
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date

class TransactionCardAdapter
    : RecyclerView.Adapter<TransactionCardAdapter.TransactionCardViewHolder>(){

    class TransactionCardViewHolder(
        val itemBinding: CardViewTransactionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)
    {
        fun bindTransaction(transaction: Transaction)
        {
            val dateFormat = SimpleDateFormat("dd/M/yyyy")
            itemBinding.transactionTitle.text = transaction.title
            itemBinding.transactionCategory.text = transaction.category.substring(0, 1).uppercase() + transaction.category.substring(1).lowercase()
            itemBinding.transactionDate.text = dateFormat.format(transaction.date)
//            itemBinding.transactionAmount.text = "IDR ${transaction.amount}"
            itemBinding.transactionAddress.text = transaction.address

            val amountColor = if (transaction.category == "EXPENSE") {
                ContextCompat.getColor(itemBinding.root.context, R.color.expenseColor)
            } else {
                ContextCompat.getColor(itemBinding.root.context, R.color.incomeColor)
            }

            itemBinding.transactionAmount.apply {
                val decimalFormat = DecimalFormat("#,##") // Format for large numbers
                val formattedAmount = decimalFormat.format(transaction.amount) // Format the amount

                text = "IDR ${formattedAmount}"
                setTextColor(amountColor)
            }
        }

    }

    private val differCallback = object : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.title == newItem.title &&
                    oldItem.amount == newItem.amount &&
                    oldItem.address == newItem.address
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }

    //    determine differences between two list in background tree
    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionCardViewHolder {
        return TransactionCardViewHolder(CardViewTransactionBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun getItemCount(): Int = differ.currentList.size

    override fun onBindViewHolder(holder: TransactionCardViewHolder, position: Int) {
        val currentTransaction = differ.currentList[position]

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        holder.itemBinding.transactionTitle.text = currentTransaction.title
        holder.itemBinding.transactionCategory.text = currentTransaction.category.substring(0, 1).uppercase() + currentTransaction.category.substring(1).lowercase()
        holder.itemBinding.transactionDate.text = dateFormat.format(currentTransaction.date)
//        holder.itemBinding.transactionAmount.text = "IDR ${currentTransaction.amount}"
        holder.itemBinding.transactionAddress.text = currentTransaction.address
        val amountColor = if (currentTransaction.category == "EXPENSE") {
            ContextCompat.getColor(holder.itemBinding.root.context, R.color.expenseColor)
        } else {
            ContextCompat.getColor(holder.itemBinding.root.context, R.color.incomeColor)
        }

        holder.itemBinding.transactionAmount.apply {
            val decimalFormat = DecimalFormat("#,###.##") // Format for large numbers
            val formattedAmount = decimalFormat.format(currentTransaction.amount) // Format the amount

            text = "IDR ${formattedAmount}"
            setTextColor(amountColor)
        }

        holder.itemView.setOnClickListener {
            val direction = NavbarFragmentDirections.actionNavbarFragmentToEditTransactionFragment2(currentTransaction)
            it.findNavController().navigate(direction)
        }
    }


}