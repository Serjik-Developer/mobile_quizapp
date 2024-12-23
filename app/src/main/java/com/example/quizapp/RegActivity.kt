package com.example.quizapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject



class RegActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reg)
        val login = findViewById<EditText>(R.id.reg_login)
        val password = findViewById<EditText>(R.id.reg_pasword)
        val RegMe = findViewById<Button>(R.id.RegMe)
        RegMe.setOnClickListener {
            val log = login.text.toString()
            val pass = password.text.toString()


            sendPostRequest(log, pass)

        }
    }
    fun showToast(context: Context, message: String) {
        // Отображаем Toast на главном потоке
        (context as? RegActivity)?.runOnUiThread {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
    fun saveTokenToSharedPreferences(context: Context, token: String) {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }

    private fun sendPostRequest(login: String, password: String) {
        val json = JSONObject()
        json.put("login", login)
        json.put("password", password)
        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/register") // Если тестируете на эмуляторе, используйте 10.0.2.2 вместо localhost
            .post(requestBody)
            .build()
        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        val token = jsonResponse.getString("token")
                        saveTokenToSharedPreferences(this, token)
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                } else {
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.optString("message") == "User already exists") {
                            // Показываем Toast, если пользователь уже существует
                            showToast(this, "Пользователь уже существует")
                        } else {
                            showToast(this, "Ошибка: ${jsonResponse.optString("message")}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    fun needLog(view: View) {startActivity(Intent(this, LoginActivity::class.java))}
}