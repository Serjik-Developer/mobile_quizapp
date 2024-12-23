package com.example.quizapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.UUID

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val login = findViewById<EditText>(R.id.log_login)
        val password = findViewById<EditText>(R.id.log_pass)
        val LogMe = findViewById<Button>(R.id.LogMe)
        LogMe.setOnClickListener {
            val pass = password.text.toString()
            val log = login.text.toString()
            sendPostRequest(log, pass)
        }

    }
    fun showToast(context: Context, message: String) {
        // Отображаем Toast на главном потоке
        (context as? LoginActivity)?.runOnUiThread {
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
            .url("https://backend-quizapp-9ad6.onrender.com/auth") // Если тестируете на эмуляторе, используйте 10.0.2.2 вместо localhost
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
                }
                else
                {
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.optString("message") == "User not found") {
                            // Показываем Toast, если пользователь уже существует
                            showToast(this, "Неправильный логин или пароль!")
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


    fun needReg(view: View) {startActivity(Intent(this,RegActivity::class.java))}
}