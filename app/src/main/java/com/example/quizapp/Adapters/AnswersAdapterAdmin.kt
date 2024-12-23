package com.example.quizapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Interfaces.OnItemClickListenerAnswers
import com.example.quizapp.Models.AnswersAdmin
import com.example.quizapp.R

class AnswersAdapterAdmin (
    private val quizList: MutableList<AnswersAdmin>,
    private val listener: OnItemClickListenerAnswers
) : RecyclerView.Adapter<AnswersAdapterAdmin.QuestionViewHolder>() {

    inner class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ans: TextView = itemView.findViewById(R.id.text_ans)
        val explain: TextView = itemView.findViewById(R.id.explain_ans)
        val trueA: TextView = itemView.findViewById(R.id.true_ans)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_btn_ans)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_btn_ans)
        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val quiz = quizList[position]
                    listener.onEditClick(quiz.aid!!, quiz.text!!, quiz.explanation!!, quiz.trueQ!!)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val answerId = quizList[position].aid!!
                    listener.onDeleteClick(answerId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.answer_rv_admin, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val quizItem = quizList[position]
        holder.ans.text = quizItem.text
        holder.explain.text = quizItem.explanation
        holder.trueA.text = quizItem.trueQ
    }

    override fun getItemCount(): Int = quizList.size
}