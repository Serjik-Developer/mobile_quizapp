package com.example.quizapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray

class QuizActivity : AppCompatActivity() {

    private val quizList = mutableListOf<String>()// Declare the quizList variable
    private var QUIZ_ID: String = ""
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)



        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "")!!
        QUIZ_ID = intent.getStringExtra("QUIZ_ID").toString()
        val QUIZ_NAME = intent.getStringExtra("QUIZ_NAME").toString()
        val QUIZ_DESC = intent.getStringExtra("QUIZ_DESC").toString()



        val btn_start = findViewById<Button>(R.id.start_quiz)
        val name_quiz = findViewById<TextView>(R.id.quiz_name)
        val ans_count = findViewById<TextView>(R.id.QuestionNumber)
        name_quiz.text = QUIZ_NAME
        ans_count.text = QUIZ_DESC
        GETALLQUESTIONSID(token, QUIZ_ID)
        btn_start.setOnClickListener {
            startActivity(Intent(this, MainQuizActivity::class.java)
                .putExtra("QUIZ_ID", QUIZ_ID)
                .putExtra("ALL_QUESTIONS_ID", ArrayList<String>(quizList)))



        }
    }

    @SuppressLint("SetTextI18n")
    private fun GETALLQUESTIONSID(token: String, id: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizQustionsForUser/$id") // Если тестируете на эмуляторе, используйте 10.0.2.2 вместо localhost
            .addHeader("Authorization", "Bearer $token")
            .build()

        Thread {
            try {
                val count_ans = findViewById<TextView>(R.id.count_ans)
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonArray = JSONArray(responseBody)

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            quizList.add(item.getString("qid"))
                        }
                        Log.w("CHECK_QUIZ_LIST1", ArrayList<String>(quizList).toString())

                        runOnUiThread {
                            when (quizList.size) {
                                1 -> {
                                    count_ans.text = "В викторине 1 вопрос"
                                }
                                2, 3, 4 -> {
                                    count_ans.text = "В викторине ${quizList.size} вопроса"
                                }
                                else -> {
                                    count_ans.text = "В викторине ${quizList.size} вопросов"
                                }
                            }

                        }
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}
