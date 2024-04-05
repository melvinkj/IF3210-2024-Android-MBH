package com.example.transactionmanagementsystem

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isNetworkAvailable(context)) {
                showDialog()
            } else {
                dismissDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(networkReceiver)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetworkInfo?.isConnected == true
    }

    private lateinit var dialog: AlertDialog

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Network Error")
        builder.setMessage("Network is unstable. Please check your connection.")

        builder.setPositiveButton("Reload") { _, _ ->
            if (isNetworkAvailable(this)) {
                dismissDialog()
            } else {
                Toast.makeText(this, "Network is still not available. Please check your connection.", Toast.LENGTH_SHORT).show()
                showDialog()
            }
        }
        dialog = builder.create()
        dialog.show()
    }

    private fun dismissDialog() {
        if (::dialog.isInitialized && dialog.isShowing) {
            dialog.dismiss()
        }
    }
}
