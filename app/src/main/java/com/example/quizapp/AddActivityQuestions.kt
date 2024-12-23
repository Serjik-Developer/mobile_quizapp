package com.example.quizapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.QuestionAdapterAdmin
import com.example.quizapp.Interfaces.OnItemClickListenerQuestions
import com.example.quizapp.Models.Questions
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

class AddActivityQuestions : AppCompatActivity(), OnItemClickListenerQuestions {
    private val questionList = mutableListOf<Questions>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestionAdapterAdmin
    private lateinit var id_main: String
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_questions)
        val btn = findViewById<Button>(R.id.btn_add_question)
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "")!!
        id_main = intent.getStringExtra("QUIZ_ID")!!
        // Инициализация RecyclerView и адаптера
        recyclerView = findViewById(R.id.rv_admin_questions)
        adapter = QuestionAdapterAdmin(questionList, this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        GETALLQUESTIONS(token,id_main)
        btn.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_add_question, null)
            val editName = view.findViewById<EditText>(R.id.edit_question_name_add)
            val radiogroup = view.findViewById<RadioGroup>(R.id.RadioGroupAdd)

            dialog.setView(view)

                .setPositiveButton("Save") { _, _ ->
                    val idb = radiogroup.checkedRadioButtonId
                    try {
                        if (editName.text.toString() == "" || editName.text.toString() == null) {
                            Toast.makeText(this, "Вы не написали вопрос!", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            val type = view.findViewById<RadioButton>(idb).text.toString()
                            val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                            val token = sharedPref.getString("auth_token", "")!!
                            val Name = editName.text.toString()
                            sendPostRequest(Name, type, token)
                        }
                    }
                    catch (Exception: Exception) {
                        Toast.makeText(this, "Вы не выбрали тип!", Toast.LENGTH_SHORT).show()
                    }

                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()

        }
    }
    private fun sendPostRequest(Name:String, type:String, token: String) {
        val client = OkHttpClient()

        // Создаём JSON объект с данными
        val jsonObject = JSONObject()
        jsonObject.put("Question", Name)
        jsonObject.put("Type", type)

        // Создаём тело запроса
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizQuestion/$id_main")
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
                    println("PUT запрос успешен: ${response.body?.string()}")
                    GETALLQUESTIONS(token, id_main)
                } else {
                    println("Ошибка PUT запроса: ${response.code}")
                }
            }
        })
    }
    override fun onItemClick(qid: String, question: String) {
        val intent = Intent(this, AddActivityAnswers::class.java)
        intent.putExtra("QUESTION_ID", qid)
        startActivity(intent)
    }

    @SuppressLint("MissingInflatedId")
    override fun onEditClick(qid: String, question: String) {
        val dialog = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_edit_question, null)
        val editName = view.findViewById<EditText>(R.id.edit_question_name)
        editName.setText(question)

        dialog.setView(view)
            .setPositiveButton("Save") { _, _ ->
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "")!!
                val updatedName = editName.text.toString()
                sendPutRequest(qid, updatedName, token)
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDeleteClick(qid: String) {
        AlertDialog.Builder(this)
            .setTitle("Delete question")
            .setMessage("Are you sure you want to delete this question?")
            .setPositiveButton("Yes") { _, _ ->
                val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
                val token = sharedPref.getString("auth_token", "")!!
                sendDeleteRequest(qid, token)
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }

    private fun GETALLQUESTIONS(token: String, QuizId:String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuestionsAdmin/$QuizId")
            .addHeader("Authorization", "Bearer $token")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)

                        val quizzes = mutableListOf<Questions>()
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val id = item.getString("id")
                            val qid = item.getString("qid")
                            val question = item.getString("question")
                            val type = item.getString("type")
                            quizzes.add(Questions(id, qid, question, type))
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
    fun sendPutRequest(qid: String, question: String,  token: String) {
        val client = OkHttpClient()

        // Создаём JSON объект с данными
        val jsonObject = JSONObject()
        jsonObject.put("question", question)

        // Создаём тело запроса
        val requestBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizQuestion/$qid")
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
                    GETALLQUESTIONS(token, id_main)
                } else {
                    println("Ошибка PUT запроса: ${response.code}")
                }
            }
        })
    }
    fun sendDeleteRequest(qid: String, token: String) {
        val client = OkHttpClient()

        // Формируем запрос
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizQuestion/$qid")
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
                    GETALLQUESTIONS(token, id_main)
                } else {
                    println("Ошибка DELETE запроса: ${response.code}")
                }
            }
        })
    }
}