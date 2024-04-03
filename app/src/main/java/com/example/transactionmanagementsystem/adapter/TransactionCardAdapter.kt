package com.example.transactionmanagementsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.transactionmanagementsystem.databinding.CardViewTransactionBinding
import com.example.transactionmanagementsystem.fragments.NavbarFragmentDirections
import com.example.transactionmanagementsystem.fragments.TransactionListFragmentDirections
import com.example.transactionmanagementsystem.models.Transaction
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
            itemBinding.transactionCategory.text = transaction.category
            itemBinding.transactionDate.text = dateFormat.format(transaction.date)
            itemBinding.transactionAmount.text = "IDR ${transaction.amount}"
            itemBinding.transactionAddress.text = transaction.address
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
        holder.itemBinding.transactionCategory.text = currentTransaction.category
        holder.itemBinding.transactionDate.text = dateFormat.format(currentTransaction.date)
        holder.itemBinding.transactionAmount.text = "IDR ${currentTransaction.amount}"
        holder.itemBinding.transactionAddress.text = currentTransaction.address

        holder.itemView.setOnClickListener {
            val direction = NavbarFragmentDirections.actionNavbarFragmentToEditTransactionFragment2(currentTransaction)
            it.findNavController().navigate(direction)
        }
    }


}