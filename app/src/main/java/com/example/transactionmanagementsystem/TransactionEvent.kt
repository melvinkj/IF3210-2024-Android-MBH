//package com.example.transactionmanagementsystem
//
//import com.example.transactionmanagementsystem.models.Transaction
//
//sealed interface TransactionEvent {
//    object SaveTransaction: TransactionEvent
//    data class SetTitle(val title: String): TransactionEvent
//    data class SetAmount(val amount: Int): TransactionEvent
//    data class SetCategory(val category: String): TransactionEvent
//    data class SetLocation(val location: String): TransactionEvent
//    object ShowDialog: TransactionEvent
//    object HideDialog: TransactionEvent
//
//    data class SortTransaction(val sortType: SortType): TransactionEvent
//    data class DeleteTransaction(val transaction: Transaction): TransactionEvent
//}
