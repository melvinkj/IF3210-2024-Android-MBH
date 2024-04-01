package com.example.transactionmanagementsystem

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.transactionmanagementsystem.adapter.TransactionCardAdapter
import com.example.transactionmanagementsystem.database.TransactionDatabase
import com.example.transactionmanagementsystem.databinding.ActivityMainBinding
import com.example.transactionmanagementsystem.repository.TransactionRepository
import com.example.transactionmanagementsystem.ui.theme.TransactionManagementSystemTheme
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModel
import com.example.transactionmanagementsystem.viewmodel.TransactionViewModelFactory

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var transactionViewModel: TransactionViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TransactionManagementSystemTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
        setContentView(R.layout.activity_main)
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
    }

    private fun setupViewModel(){
        val transactionRepository = TransactionRepository(TransactionDatabase(this))
        val viewModelProviderFactory = TransactionViewModelFactory(application, transactionRepository)
        transactionViewModel = ViewModelProvider(this, viewModelProviderFactory)[TransactionViewModel::class.java]
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TransactionManagementSystemTheme {
        Greeting("Android")
    }
}
