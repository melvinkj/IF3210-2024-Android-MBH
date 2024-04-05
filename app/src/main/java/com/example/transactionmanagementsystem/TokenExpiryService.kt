package com.example.transactionmanagementsystem

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.widget.Toast
import com.example.transactionmanagementsystem.api.ApiInterface
import com.example.transactionmanagementsystem.api.RetrofitInstance
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenExpiryService : Service() {
    private val handler = Handler()
    private val runnableCode = object : Runnable {
        override fun run() {
            checkJWT()

            // Run again after 1 minute
            handler.postDelayed(this, 1 * 60 * 1000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(runnableCode)
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun checkJWT() {
        val token = getSharedPreferences("UserToken", MODE_PRIVATE).getString("token", null)
        if (token != null) {
            val retIn = RetrofitInstance.getRetrofitInstance().create(ApiInterface::class.java)

            val call = retIn.checkToken("Bearer $token")
            call.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        this@TokenExpiryService,
                        t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    println(response)
                    if (response.code() == 401) {
                        logout()
                    }
                }
            })
        } else {
            // Stop the TokenExpiryService
            val serviceIntent = Intent(this, TokenExpiryService::class.java)
            stopService(serviceIntent)

            // Redirect to the login page
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
