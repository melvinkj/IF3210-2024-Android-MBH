package com.example.transactionmanagementsystem

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.transactionmanagementsystem.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.transactionmanagementsystem.adapter.TransactionCardAdapter
import com.example.transactionmanagementsystem.database.TransactionDatabase
import com.example.transactionmanagementsystem.repository.TransactionRepository
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModelFactory


class MainActivity : BaseActivity() {

//    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding : ActivityMainBinding

    lateinit var transactionViewModel: TransactionViewModel

    private fun setupViewModel(){
        val transactionRepository = TransactionRepository(TransactionDatabase(this))
        val viewModelProviderFactory = TransactionViewModelFactory(application, transactionRepository)
        transactionViewModel = ViewModelProvider(this, viewModelProviderFactory)[TransactionViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.navbar.setBackgroundColor(0xFFFFFFF.toInt())
        }

        setContentView(binding.root)
        setupViewModel()
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val mainActivity = this
//
//        binding.recyclerview.apply {
//            layoutManager = LinearLayoutManager(applicationContext)
//            adapter = TransactionCardAdapter()
//        }

        supportActionBar?.hide()
//        bottomNavigationView = findViewById(R.id.navbar)

        replaceFragment(TransactionFragment())

        binding.navbar.setOnItemSelectedListener{
            when(it.itemId){
                R.id.transaction -> replaceFragment(TransactionFragment())
                R.id.scan -> replaceFragment(ScanFragment())
                R.id.graph -> replaceFragment(GraphFragment())
                R.id.settings -> replaceFragment(SettingsFragment())
            else -> {}
            }
            true

        }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.commit()
    }


}
