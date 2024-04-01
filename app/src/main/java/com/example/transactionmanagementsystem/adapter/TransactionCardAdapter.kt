package com.example.transactionmanagementsystem.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.transactionmanagementsystem.databinding.CardViewTransactionBinding
import com.example.transactionmanagementsystem.fragments.TransactionListFragmentDirections
import com.example.transactionmanagementsystem.models.Transaction

class TransactionCardAdapter
    : RecyclerView.Adapter<TransactionCardAdapter.TransactionCardViewHolder>(){

    class TransactionCardViewHolder(
        val itemBinding: CardViewTransactionBinding
    ) : RecyclerView.ViewHolder(itemBinding.root)
    {
        fun bindTransaction(transaction: Transaction)
        {
            itemBinding.transactionTitle.text = transaction.title
            itemBinding.transactionCategory.text = transaction.category
            itemBinding.transactionDate.text = transaction.date.toString()
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

        holder.itemBinding.transactionTitle.text = currentTransaction.title
        holder.itemBinding.transactionCategory.text = currentTransaction.category
        holder.itemBinding.transactionDate.text = currentTransaction.date.toString()
        holder.itemBinding.transactionAmount.text = "IDR ${currentTransaction.amount}"
        holder.itemBinding.transactionAddress.text = currentTransaction.address

        holder.itemView.setOnClickListener {
            val direction = TransactionListFragmentDirections.actionTransactionListFragmentToEditTransactionFragment(currentTransaction)
            it.findNavController().navigate(direction)
        }
    }


}