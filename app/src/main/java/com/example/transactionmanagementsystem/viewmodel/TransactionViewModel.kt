@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.transactionmanagementsystem.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.example.transactionmanagementsystem.SortType
//import com.example.transactionmanagementsystem.TransactionEvent
//import com.example.transactionmanagementsystem.TransactionState
//import com.example.transactionmanagementsystem.database.TransactionDao
import com.example.transactionmanagementsystem.models.Transaction
import com.example.transactionmanagementsystem.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.flow.flatMapLatest
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class TransactionViewModel(
//    private val dao: TransactionDao
//): ViewModel() {
//
//    private val _sortType = MutableStateFlow(SortType.DATE)
//    private val _transactions = _sortType
//        .flatMapLatest { sortType ->
//            when(sortType){
//                SortType.TITLE -> dao.getTransactionsOrderedByTitle()
//                SortType.CATEGORY -> dao.getTransactionsOrderedByCategory()
//                SortType.AMOUNT -> dao.getTransactionsOrderedByAmount()
//                SortType.LOCATION -> dao.getTransactionsOrderedByLocation()
//                SortType.DATE -> dao.getTransactionsOrderedByDate()
//            }
//        }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
//    private val _state = MutableStateFlow(TransactionState())
//    val state = combine(_state, _sortType, _transactions) { state, sortType, transactions ->
//        state.copy(
//            transactions = transactions,
//            sortType = sortType
//        )
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionState())
//
//
//    fun onEvent(event: TransactionEvent){
//        when(event) {
//            is TransactionEvent.DeleteTransaction -> {
//                viewModelScope.launch {
//                    dao.deleteTransaction(event.transaction)
//                }
//            }
//            TransactionEvent.HideDialog -> {
//                _state.update { it.copy(
//                    isAddingTransaction = false
//                ) }
//            }
//            TransactionEvent.SaveTransaction -> {
//                val title = state.value.title
//                val amount = state.value.amount
//                val category = state.value.category
//                val location = state.value.location
//                val date = state.value.date
//
//                if(title.isBlank() || amount.equals(0) || category.isBlank() || location.isBlank() || date.isBlank()) {
//                    return
//                }
//                val transaction = Transaction(
//                    title = title,
//                    amount = amount,
//                    category = category,
//                    location = location,
//                    date = date
//                )
//                viewModelScope.launch {
//                    dao.upsertTransaction(transaction)
//                }
//                _state.update { it.copy(
//                    isAddingTransaction = false,
//                    title = "",
//                    amount = 0,
//                    category = "",
//                    location = "",
//                    date = ""
//                ) }
//
//
//
//            }
//            is TransactionEvent.SetTitle -> {
//                _state.update { it.copy(
//                    title = event.title
//                ) }
//            }
//            is TransactionEvent.SetAmount -> {
//                _state.update { it.copy(
//                    amount = event.amount
//                ) }
//            }
//            is TransactionEvent.SetCategory -> {
//                _state.update { it.copy(
//                    category = event.category
//                ) }
//            }
//            is TransactionEvent.SetLocation -> {
//                _state.update { it.copy(
//                    location = event.location
//                ) }
//            }
//            TransactionEvent.ShowDialog -> {
//                _state.update { it.copy(
//                    isAddingTransaction = true
//                ) }
//            }
//            is TransactionEvent.SortTransaction -> {
//                _sortType.value = event.sortType
//            }
//        }
//    }
//}

class TransactionViewModel (app: Application, private val transactionRepository: TransactionRepository) : AndroidViewModel(app) {

    fun addTransaction(transaction: Transaction) =
        viewModelScope.launch {
            transactionRepository.upsertTransaction(transaction)
        }

    fun editTransaction(transaction: Transaction) =
        viewModelScope.launch {
            transactionRepository.upsertTransaction(transaction)
        }
    fun deleteTransaction(transaction: Transaction) =
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    fun getTransactionsOrderedById() = transactionRepository.getTransactionsOrderedById()
    fun getTransactionsOrderedByTitle() = transactionRepository.getTransactionsOrderedByTitle()
    fun getTransactionsOrderedByAmount() = transactionRepository.getTransactionsOrderedByAmount()
    fun getTransactionsOrderedByCategory() = transactionRepository.getTransactionsOrderedByCategory()
    fun getTransactionsOrderedByLocation() = transactionRepository.getTransactionsOrderedByLocation()
    fun getTransactionsOrderedByDate() = transactionRepository.getTransactionsOrderedByDate()

    fun searchTransaction(query: String?) : Flow<List<Transaction>> = transactionRepository.searchTransaction(query)
}