package com.example.quizapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Interfaces.OnItemClickListenerQuestions
import com.example.quizapp.Models.Questions
import com.example.quizapp.Models.QuizMain
import com.example.quizapp.R

class QuestionAdapterAdmin(
    private val quizList: MutableList<Questions>,
    private val listener: OnItemClickListenerQuestions
) : RecyclerView.Adapter<QuestionAdapterAdmin.QuestionViewHolder>() {

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.question_name)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_btn_question)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_btn_question)
        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val quiz = quizList[position]
                    listener.onItemClick(quiz.qid!!, quiz.question!!)
                }
            }
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val quiz = quizList[position]
                    listener.onEditClick(quiz.qid!!, quiz.question!!)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val quizId = quizList[position].qid!!
                    listener.onDeleteClick(quizId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_rv_admin, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val quizItem = quizList[position]
        holder.textView.text = quizItem.question
    }

    override fun getItemCount(): Int = quizList.size
}