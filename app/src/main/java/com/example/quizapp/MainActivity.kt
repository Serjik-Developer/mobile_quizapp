package com.example.quizapp
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Adapters.QuizAdapter
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Models.QuizMain
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
class MainActivity : AppCompatActivity(), OnItemClickListener {
    @SuppressLint("MissingInflatedId")
    private val quizList = mutableListOf<QuizMain>()
    private lateinit var quizAdapter: QuizAdapter
    private lateinit var login_user: TextView
    private lateinit var exp_user: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        login_user = findViewById(R.id.login_user)
        exp_user = findViewById(R.id.exp_user)
        val logout = findViewById<ImageButton>(R.id.log_out)
        val sharedPref = getSharedPreferences("AppPreferences", MODE_PRIVATE)
        val token = sharedPref.getString("auth_token", "")!!
        if (token == null || token == "") startActivity(Intent(this, LoginActivity::class.java))
        GETINFOABOUTUSER(token)
        GETALLQUIZ(token)
        logout.setOnClickListener {
            sharedPref.edit().putString("auth_token", "").commit()
            startActivity(Intent(this, LoginActivity::class.java))
        }
        val btn = findViewById<ImageButton>(R.id.addBtn)
        btn.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }

        val RVEnable = findViewById<RecyclerView>(R.id.RVEnable)
        RVEnable.layoutManager = LinearLayoutManager(this)
        quizAdapter = QuizAdapter(quizList, this)
        RVEnable.adapter = quizAdapter
    }

    private fun GETINFOABOUTUSER(token: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://backend-quizapp-9ad6.onrender.com/user")
            .addHeader("Authorization", "Bearer $token")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        val login = jsonResponse.getString("login")
                        val exp = jsonResponse.getString("exp")
                        val role = jsonResponse.getString("role")
                        val btn = findViewById<ImageButton>(R.id.addBtn)
                        runOnUiThread {
                            if (role!="admin") btn.visibility = View.GONE
                            login_user.text = "Привет $login"
                            exp_user.text = "У вас $exp очков!"
                        }
                    }
                } else {
                    response.body?.string()?.let { responseBody ->
                        val jsonResponse = JSONObject(responseBody)
                        if (jsonResponse.optString("message") == "Not authorized") {
                            runOnUiThread {
                                startActivity(Intent(this, LoginActivity::class.java))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    @SuppressLint("NotifyDataSetChanged")
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
                        }

                        runOnUiThread {
                            quizList.clear()
                            quizList.addAll(quizzes)
                            quizAdapter.notifyDataSetChanged()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onItemClick(quizId: String, quizName: String, quizDesc: String) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("QUIZ_ID", quizId)
            .putExtra("QUIZ_NAME", quizName)
            .putExtra("QUIZ_DESC", quizDesc)
        startActivity(intent)
    }
    override fun onEditClick(quizId: String, quizName: String, quizDesc: String) {}
    override fun onDeleteClick(quizId: String) {}
    fun addact(view: View) {
        startActivity(Intent(this, AddActivity::class.java))
    }
}
