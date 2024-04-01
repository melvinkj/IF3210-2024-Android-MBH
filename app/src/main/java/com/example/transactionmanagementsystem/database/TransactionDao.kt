package com.example.transactionmanagementsystem.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.transactionmanagementsystem.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Upsert
    suspend fun upsertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getTransactionsOrderedById(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions ORDER BY title ASC")
    fun getTransactionsOrderedByTitle(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions ORDER BY amount ASC")
    fun getTransactionsOrderedByAmount(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions ORDER BY category ASC")
    fun getTransactionsOrderedByCategory(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions ORDER BY address ASC")
    fun getTransactionsOrderedByLocation(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions ORDER BY date ASC")
    fun getTransactionsOrderedByDate(): Flow<List<Transaction>>
    @Query("SELECT * FROM transactions WHERE title LIKE :query OR category LIKE :query OR address LIKE :query")
    fun searchTransaction(query: String?): Flow<List<Transaction>>

}