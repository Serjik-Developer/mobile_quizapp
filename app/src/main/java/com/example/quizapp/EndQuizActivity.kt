package com.example.quizapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EndQuizActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_end_quiz)
        val exp = intent.getIntExtra("EXP", 0)
        val count = findViewById<TextView>(R.id.CountTrue)
        val btn = findViewById<Button>(R.id.return_btn)
        when (exp) {
            1 -> {
                count.text = "Вы набрали 1 балл!"
            }
            2, 3, 4 -> {
                count.text = "Вы набрали ${exp.toString()} балла!"
            }
            else -> {
                count.text = "Вы набрали ${exp.toString()} баллов!"
            }
        }
        btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}