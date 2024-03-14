package com.example.transactionmanagementsystem

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.transactionmanagementsystem.databinding.ActivityMainBinding
import com.example.transactionmanagementsystem.ui.theme.TransactionManagementSystemTheme
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

//    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.navbar.setBackgroundColor(0xFFFFFFF.toInt())
        }

        setContent {
            TransactionManagementSystemTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }
        setContentView(binding.root)
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