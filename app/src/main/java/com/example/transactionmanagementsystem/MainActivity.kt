package com.example.transactionmanagementsystem

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import com.example.transactionmanagementsystem.databinding.ActivityMainBinding
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.transactionmanagementsystem.database.TransactionDatabase
import com.example.transactionmanagementsystem.fragments.AddTransactionFragment
import com.example.transactionmanagementsystem.repository.TransactionRepository
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModelFactory


class MainActivity : BaseActivity() {

    private lateinit var binding : ActivityMainBinding

    lateinit var transactionViewModel: TransactionViewModel

    private val randomizeTransactionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Start the fragment with the launch source argument
            val fragment = AddTransactionFragment.newInstance("broadcast")
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainerView, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setupViewModel()

        // Register the broadcast receiver
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(randomizeTransactionReceiver, IntentFilter("RANDOMIZE_TRANSACTION"))
    }
    private fun setupViewModel(){
        val transactionRepository = TransactionRepository(TransactionDatabase(this))
        val viewModelProviderFactory = TransactionViewModelFactory(application, transactionRepository)
        transactionViewModel = ViewModelProvider(this, viewModelProviderFactory)[TransactionViewModel::class.java]
    }
    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(randomizeTransactionReceiver)
        super.onDestroy()
    }
}
