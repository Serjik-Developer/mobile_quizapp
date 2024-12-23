package com.example.quizapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.QuizAdapterAdmin
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Models.QuizMain
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AddActivity : AppCompatActivity(), OnItemClickListener {
    private val quizList = mutableListOf<QuizMain>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuizAdapterAdmin

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "")!!
        val btn = findViewById<Button>(R.id.btn_add_quiz)
        // Инициализация RecyclerView и адаптера
        recyclerView = findViewById(R.id.rv_admin)
        adapter = QuizAdapterAdmin(quizList, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        GETALLQUIZ(token)
        btn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_edit_quiz, null)
            val editName = view.findViewById<EditText>(R.id.edit_quiz_name)
            val editDesc = view.findViewById<EditText>(R.id.edit_quiz_desc)
            dialog.setView(view)
                .setPositiveButton("Save") { _, _ ->

                    val text = editName.text.toString()
                    val desc = editDesc.text.toString()
                    sendPostRequest(text, desc, token)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()

        }
    }
    private fun sendPostRequest(text:String, desc:String, token: String) {
        val client = OkHttpClient()

        // Создаём JSON объект с данными
        val jsonObject = JSONObject()
        jsonObject.put("Text", text)
        jsonObject.put("Description", desc)

        // Создаём тело запроса
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/Quiz")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()

        // Отправляем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка ответа
                if (response.isSuccessful) {
                    println("POST запрос успешен: ${response.body?.string()}")
                    GETALLQUIZ(token)
                } else {
                    println("Ошибка POST запроса: ${response.code}")
                }
            }
        })
    }
    override fun onItemClick(quizId: String, quizName: String, quizDesc: String) {
        val intent = Intent(this, AddActivityQuestions::class.java)
        intent.putExtra("QUIZ_ID", quizId)
        startActivity(intent)
    }

    @SuppressLint("MissingInflatedId")
    override fun onEditClick(quizId: String, quizName: String, quizDesc: String) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_quiz, null)
        val editName = view.findViewById<EditText>(R.id.edit_quiz_name)
        val editDesc = view.findViewById<EditText>(R.id.edit_quiz_desc)
        editName.setText(quizName)
        editDesc.setText(quizDesc)

        dialog.setView(view)
            .setPositiveButton("Save") { _, _ ->
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "")!!
                val updatedName = editName.text.toString()
                val updatedDesc = editDesc.text.toString()
                sendPutRequest(quizId, updatedName, updatedDesc, token)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDeleteClick(quizId: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete Quiz")
            .setMessage("Are you sure you want to delete this quiz?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "")!!
                sendDeleteRequest(quizId, token)
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    private fun GETALLQUIZ(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/Quiz")
            .addHeader("Authorization", "Bearer $token")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)

                        val quizzes = mutableListOf<QuizMain>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val id = item.getString("Id")
                            val text = item.getString("Text")
                            val desc = item.getString("Description")
                            quizzes.add(QuizMain(id, text, desc))
                            Log.w("TEST", quizzes.toString())
                        }

                        runOnUiThread {
                            quizList.clear()
                            quizList.addAll(quizzes)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    fun sendPutRequest(id: String, name: String, description: String, token: String) {
        val client = OkHttpClient()

        // Создаём JSON объект с данными
        val jsonObject = JSONObject()
        jsonObject.put("Text", name)
        jsonObject.put("Description", description)

        // Создаём тело запроса
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/Quiz/$id")
            .addHeader("Authorization", "Bearer $token")
            .put(requestBody)
            .build()

        // Отправляем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка ответа
                if (response.isSuccessful) {
                    println("PUT запрос успешен: ${response.body?.string()}")
                    GETALLQUIZ(token)
                } else {
                    println("Ошибка PUT запроса: ${response.code}")
                }
            }
        })
    }
    fun sendDeleteRequest(id: String, token: String) {
        val client = OkHttpClient()

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/Quiz/$id")
            .addHeader("Authorization", "Bearer $token")
            .delete()
            .build()

        // Отправляем запрос
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибки
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // Обработка ответа
                if (response.isSuccessful) {
                    println("DELETE запрос успешен: ${response.body?.string()}")
                    GETALLQUIZ(token)
                } else {
                    println("Ошибка DELETE запроса: ${response.code}")
                }
            }
        })
    }

}