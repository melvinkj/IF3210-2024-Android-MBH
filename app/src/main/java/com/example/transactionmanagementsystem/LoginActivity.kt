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
import org.json.JSONObject

class LoginActivity : BaseActivity() {
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

                    // Save email after successful login
                    val emailPref = getSharedPreferences("Email", MODE_PRIVATE)
                    val editorEmail = emailPref.edit()
                    editorEmail.putString("email", email)
                    editorEmail.apply()
                    
                    // Save token after successful login
                    val jsonObject = JSONObject(responseBody)
                    val tokenString = jsonObject.getString("token")

                    val tokenPref = getSharedPreferences("UserToken", MODE_PRIVATE)
                    val editorToken = tokenPref.edit()
                    editorToken.putString("token", tokenString)
                    editorToken.apply()

                    // Start the TokenExpiryService
                    val serviceIntent = Intent(this@LoginActivity, TokenExpiryService::class.java)
                    startService(serviceIntent)

                    // Redirect to settings screen
//                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
//                    startActivity(intent)

                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    applicationContext.startActivity(intent)

                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed!", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
