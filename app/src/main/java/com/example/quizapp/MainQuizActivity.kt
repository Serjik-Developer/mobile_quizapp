package com.example.quizapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizapp.Models.Answers
import com.example.quizapp.Models.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject

class MainQuizActivity : AppCompatActivity() {
    private var Quiz_number: Int = 0
    private var type_c: String = ""
    private var responseAns: Response = Response()
    private var IS_answer: Boolean = false
    private var answers_list = mutableListOf<Answers>()
    private var full_exp:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_quiz)
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "")!!
        val next = findViewById<Button>(R.id.next_ans)
        val check = findViewById<Button>(R.id.check_ans)
        val Question = findViewById<TextView>(R.id.Question)
        val QUIZ_ID = intent.getStringExtra("QUIZ_ID")!!
        val ALL_QUESTIONS_ID = intent.getStringArrayListExtra("ALL_QUESTIONS_ID")!!
        GETQUESTANDANS(token, ALL_QUESTIONS_ID.first())

        next.setOnClickListener {
            Log.w("CHECK1", Quiz_number.toString())
            Log.w("CHECK2", ALL_QUESTIONS_ID.toString())


            if (Quiz_number==ALL_QUESTIONS_ID.size) {

                startActivity(Intent(this, EndQuizActivity::class.java).putExtra("EXP", full_exp))
            }
            else {
                GETQUESTANDANS(token, ALL_QUESTIONS_ID[Quiz_number].toString())
            }



        }
        check.setOnClickListener {
            if (IS_answer) {
                Toast.makeText(this, "Вы уже отвечали на вопрос!", Toast.LENGTH_SHORT).show()
            }
            else {
                sendAns(token, answers_list[0].aid!!)
            }

        }
    }



    private fun GETQUESTANDANS(token: String, qid: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizQustionForUser/$qid")
            .addHeader("Authorization", "Bearer $token")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    IS_answer=false
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        val textQ = jsonResponse.getString("textQ")
                        val type = jsonResponse.getString("type")
                        type_c = type

                        // Очистка предыдущих ответов
                        answers_list.clear()

                        // Проверка типа поля "answers"
                        val answers = jsonResponse.get("answers")
                        if (answers is JSONArray) {
                            // Если это массив, обрабатываем как массив
                            for (i in 0 until answers.length()) {
                                val answerObject = answers.getJSONObject(i)
                                val text = answerObject.getString("text")
                                val aid = answerObject.getString("aid")
                                answers_list.add(Answers(text, aid))
                            }
                        } else if (answers is JSONObject) {
                            // Если это объект, обрабатываем как объект
                            val text = answers.getString("text")
                            val aid = answers.getString("aid")
                            answers_list.add(Answers(text, aid))
                        }

                        // Обновление интерфейса
                        runOnUiThread {

                            if (answers_list.isEmpty()) {
                                findViewById<TextView>(R.id.Question).text = "No answers available"
                            } else {
                                WRITEALL(type, answers_list)
                            }
                        }
                        Log.w("ANSWER_LIST", answers_list.toString())
                        Quiz_number++
                    }
                } else {
                    runOnUiThread {
                        findViewById<TextView>(R.id.Question).text = "Failed to load question"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    findViewById<TextView>(R.id.Question).text = "Error: ${e.message}"
                }
            }
        }.start()
    }


    private fun WRITEALL(type: String, ans: MutableList<Answers>) {
        val status_ans = findViewById<TextView>(R.id.status_ans)
        val Question = findViewById<TextView>(R.id.Question)
        val InputString = findViewById<EditText>(R.id.InputString)
        val InputInt = findViewById<EditText>(R.id.InputInt)
        val RadioGroup = findViewById<RadioGroup>(R.id.RadioGroup)
        val explain = findViewById<TextView>(R.id.explanation)
        explain.visibility = View.GONE
        status_ans.visibility = View.GONE
        when (type) {
            "InputString" -> {
                InputString.visibility = View.VISIBLE
                InputInt.visibility = View.GONE
                RadioGroup.visibility = View.GONE
                Question.text = ans[0].text
                InputString.isEnabled=true
            }
            "InputInt" -> {
                InputString.visibility = View.GONE
                InputInt.visibility = View.VISIBLE
                RadioGroup.visibility = View.GONE
                Question.text = ans[0].text
                InputInt.isEnabled=true
            }
            "RadioButton" -> {
                InputString.visibility = View.GONE
                InputInt.visibility = View.GONE
                RadioGroup.visibility = View.VISIBLE




                // Очистка предыдущих RadioButton
                RadioGroup.removeAllViews()


                for (text in ans) {
                    val radioButton = RadioButton(this).apply {
                        this.text = text.text
                        this.id = View.generateViewId()
                    }
                    RadioGroup.addView(radioButton)
                }
                for (i in 0 until RadioGroup.childCount) {
                    val child = RadioGroup.getChildAt(i)
                    if (child is RadioButton) {
                        child.isEnabled=true
                    }
                }
                Question.text = ans[0].text
            }
        }
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    private fun sendAns(token: String,  aid:String) {
        var AnswerUser = ""
        val status_ans = findViewById<TextView>(R.id.status_ans)
        val InputString = findViewById<EditText>(R.id.InputString)
        val InputInt = findViewById<EditText>(R.id.InputInt)
        val RadioGroup = findViewById<RadioGroup>(R.id.RadioGroup)

        if (type_c == "InputString") {
            if (InputString.text.toString() == ""||InputString.text.toString()==null){
                Toast.makeText(this, "Вы не ответили на вопрос!", Toast.LENGTH_SHORT).show()
            }
            else {
                AnswerUser = InputString.text.toString()
            }

        } else if (type_c == "InputInt") {
            if (InputInt.text.toString() == ""||InputInt.text.toString()==null){
                Toast.makeText(this, "Вы не ответили на вопрос!", Toast.LENGTH_SHORT).show()
            }
            else{
                AnswerUser = InputInt.text.toString()
            }

        } else if (type_c == "RadioButton") {
            val idb = RadioGroup.checkedRadioButtonId
            try {
                AnswerUser = findViewById<RadioButton>(idb).text.toString()
            }
            catch (Exception: Exception) {
                Toast.makeText(this, "Вы не ответили на вопрос!", Toast.LENGTH_SHORT).show()
            }



        }

        val json = JSONObject()
        json.put("AnswerUser", AnswerUser)

        val requestBody = RequestBody.create(
            "application/json".toMediaTypeOrNull(),
            json.toString()
        )
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/QuizAnswerForUser/$aid")
            .addHeader("Authorization", "Bearer $token")
            .post(requestBody)
            .build()


        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    IS_answer=true
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        val status = jsonResponse.getString("status")
                        val explanation = jsonResponse.getString("explanation")

                        val correct = jsonResponse.getString("correct")
                        val your = jsonResponse.getString("your")
                        responseAns = Response(status, explanation, correct, your)
                        runOnUiThread {
                            val explain = findViewById<TextView>(R.id.explanation)
                            if (explanation !="" || explanation!=null) {
                                explain.visibility = View.VISIBLE
                                explain.text = explanation
                            }
                            if (status=="GOOD") {
                                full_exp++
                                when (type_c) {
                                    "InputString" -> {
                                        InputString.setBackgroundColor(Color.GREEN)
                                        InputString.isEnabled=false
                                    }
                                    "InputInt" -> {
                                        InputInt.setBackgroundColor(Color.GREEN)
                                        InputInt.isEnabled=false
                                    }
                                    "RadioButton" -> {
                                        val idb = RadioGroup.checkedRadioButtonId
                                        findViewById<RadioButton>(idb).setBackgroundColor(Color.GREEN)
                                        for (i in 0 until RadioGroup.childCount) {
                                            val child = RadioGroup.getChildAt(i)
                                            if (child is RadioButton) {
                                                child.isEnabled=false
                                            }
                                        }
                                    }
                                }
                            }
                            else if (status=="BAD") {
                                when (type_c) {
                                    "InputString" -> {
                                        InputString.setBackgroundColor(Color.RED)
                                        InputString.isEnabled=false
                                    }
                                    "InputInt" -> {
                                        InputInt.setBackgroundColor(Color.RED)
                                        InputInt.isEnabled=false
                                    }
                                    "RadioButton" -> {
                                        val idb = RadioGroup.checkedRadioButtonId
                                        findViewById<RadioButton>(idb).setBackgroundColor(Color.RED)
                                        for (i in 0 until RadioGroup.childCount) {
                                            val child = RadioGroup.getChildAt(i)
                                            if (child is RadioButton) {
                                                child.isEnabled=false
                                                if (child.text.toString() == correct) {
                                                    child.setBackgroundColor(Color.GREEN)
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else{

                            }
                            runOnUiThread {
                                status_ans.visibility = View.VISIBLE
                                if (status=="GOOD") {
                                    status_ans.text = "Правильно! Молодец! Так держать!"
                                }
                                else if (status=="BAD") {
                                    status_ans.text = "Неправильно! Правильный вариант ответа :${correct}"
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
