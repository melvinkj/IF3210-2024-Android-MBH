package com.example.transactionmanagementsystem

import android.content.Intent
import android.widget.Toast
import com.example.transactionmanagementsystem.api.ApiInterface
import com.example.transactionmanagementsystem.api.RetrofitInstance
import com.example.transactionmanagementsystem.models.LoginRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.login)

        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            signin(email, password)
        }
    }

    private fun signin(email: String, password: String){
        val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)
        val signInInfo = LoginRequest(email, password)

        retIn.signin(signInInfo).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    this@LoginActivity,
                    t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.code() == 200) { 
                    Toast.makeText(this@LoginActivity, "Login success!", Toast.LENGTH_SHORT).show()
                    val responseBody = response.body()?.string()
                    println("Response Body: " + responseBody)
                    
                    // Save token after successful login
                    val token = getSharedPreferences("UserToken", MODE_PRIVATE)
                    val editor = token.edit()
                    editor.putString("token", responseBody)
                    editor.apply()

                    // Redirect to settings screen
                    val intent = Intent(this@LoginActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
