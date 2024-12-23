package com.example.quizapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.AnswersAdapterAdmin
import com.example.quizapp.Interfaces.OnItemClickListenerAnswers
import com.example.quizapp.Models.AnswersAdmin
import com.example.quizapp.Models.Questions
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

class AddActivityAnswers : AppCompatActivity(), OnItemClickListenerAnswers {
    private val questionList = mutableListOf<AnswersAdmin>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AnswersAdapterAdmin
    private lateinit var id_main: String
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_answers)
        val btn = findViewById<Button>(R.id.btn_add_answers)
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "")!!
        id_main = intent.getStringExtra("QUESTION_ID")!!
        // Инициализация RecyclerView и адаптера
        recyclerView = findViewById(R.id.rv_answers)
        adapter = AnswersAdapterAdmin(questionList, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        GETALLANSWERS(token,id_main)
        btn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_edit_answers, null)
            val editName = view.findViewById<EditText>(R.id.edit_answer_name)
            val editExplain = view.findViewById<EditText>(R.id.edit_answer_explanation)
            val editTrue = view.findViewById<EditText>(R.id.edit_answer_true)


            dialog.setView(view)
                .setPositiveButton("Save") { _, _ ->
                    val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                    val token = sharedPref.getString("auth_token", "")!!
                    val Name = editName.text.toString()
                    val Explain = editExplain.text.toString()
                    val Correct = editTrue.text.toString()
                    sendPostRequest(Name,  Explain, Correct, token)
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()

        }
    }
    private fun sendPostRequest(Name:String, Explain:String, Correct:String, token: String) {
        val client = OkHttpClient()

        // Создаём JSON объект с данными
        val jsonObject = JSONObject()
        jsonObject.put("Text", Name)
        jsonObject.put("Explanation", Explain)
        jsonObject.put("True", Correct)

        // Создаём тело запроса
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizAnswer/$id_main")
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
                    GETALLANSWERS(token, id_main)
                } else {
                    println("Ошибка POST запроса: ${response.code}")
                }
            }
        })
    }



    @SuppressLint("MissingInflatedId")
    override fun onEditClick(aid: String, text: String, explanation:String, trueQustion: String) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_answers, null)
        val editName = view.findViewById<EditText>(R.id.edit_answer_name)
        val editExplain = view.findViewById<EditText>(R.id.edit_answer_explanation)
        val editTrue = view.findViewById<EditText>(R.id.edit_answer_true)
        editName.setText(text)
        editExplain.setText(explanation)
        editTrue.setText(trueQustion)

        dialog.setView(view)
            .setPositiveButton("Save") { _, _ ->
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "")!!
                val updatedName = editName.text.toString()
                val updatedExplain = editExplain.text.toString()
                val updatedCorrect = editTrue.text.toString()
                sendPutRequest(aid, updatedName,  updatedExplain, updatedCorrect, token)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDeleteClick(aid: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete answer")
            .setMessage("Are you sure you want to delete this answer?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "")!!
                sendDeleteRequest(aid, token)
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    private fun GETALLANSWERS(token: String, QuestionID:String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/AnswersAdmin/$QuestionID")
            .addHeader("Authorization", "Bearer $token")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)

                        val quizzes = mutableListOf<AnswersAdmin>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val aid = item.getString("aid")
                            val qid = item.getString("qid")
                            val text = item.getString("text")
                            val explanation = item.getString("explanation")
                            val trueQ = item.getString("true")
                            quizzes.add(AnswersAdmin(qid, aid, text, explanation, trueQ))
                            Log.w("TEST", quizzes.toString())
                        }

                        runOnUiThread {
                            questionList.clear()
                            questionList.addAll(quizzes)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
    fun sendPutRequest(aid: String, text: String,  explanation: String, trueQustion:String, token:String) {
        val client = OkHttpClient()

        // Создаём JSON объект с данными
        val jsonObject = JSONObject()
        jsonObject.put("text", text)
        jsonObject.put("explanation", explanation)
        jsonObject.put("trueQustion", trueQustion)

        // Создаём тело запроса
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizAnswer/$aid")
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
                    GETALLANSWERS(token, id_main)
                } else {
                    println("Ошибка PUT запроса: ${response.code}")
                }
            }
        })
    }
    fun sendDeleteRequest(aid: String, token: String) {
        val client = OkHttpClient()

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizAnswer/$aid")
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
                    GETALLANSWERS(token, id_main)
                } else {
                    println("Ошибка DELETE запроса: ${response.code}")
                }
            }
        })
    }
}