package com.example.transactionmanagementsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SettingsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val logoutButton = findViewById<Button>(R.id.logout)

        logoutButton.setOnClickListener {
            logout()
        }
    }

    private fun logout() {
        // Clear the saved token
        val token = getSharedPreferences("UserToken", MODE_PRIVATE)
        val editor = token.edit()
        editor.remove("token")
        editor.apply()

        // Stop the TokenExpiryService
        val serviceIntent = Intent(this, TokenExpiryService::class.java)
        stopService(serviceIntent)

        // Redirect to the login page
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
