package com.example.transactionmanagementsystem.viewmodel;


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.transactionmanagementsystem.repository.TransactionRepository

class TransactionViewModelFactory(val app: Application, private val transactionRepository:TransactionRepository) :  ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransactionViewModel(app, transactionRepository) as T
    }



}
