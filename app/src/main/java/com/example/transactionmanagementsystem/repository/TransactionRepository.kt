package com.example.transactionmanagementsystem.repository

import androidx.room.Query
import com.example.transactionmanagementsystem.database.TransactionDatabase
import com.example.transactionmanagementsystem.models.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val db: TransactionDatabase) {
    suspend fun upsertTransaction(transaction: Transaction) = db.getTransactionDao().upsertTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = db.getTransactionDao().deleteTransaction(transaction)
    fun getTransactionsOrderedById() = db.getTransactionDao().getTransactionsOrderedById()
    fun getTransactionsOrderedByTitle() = db.getTransactionDao().getTransactionsOrderedByTitle()
    fun getTransactionsOrderedByAmount() = db.getTransactionDao().getTransactionsOrderedByAmount()
    fun getTransactionsOrderedByCategory() = db.getTransactionDao().getTransactionsOrderedByCategory()
    fun getTransactionsOrderedByLocation() = db.getTransactionDao().getTransactionsOrderedByLocation()
    fun getTransactionsOrderedByDate() = db.getTransactionDao().getTransactionsOrderedByDate()
    fun searchTransaction(query: String?) = db.getTransactionDao().searchTransaction(query)

}