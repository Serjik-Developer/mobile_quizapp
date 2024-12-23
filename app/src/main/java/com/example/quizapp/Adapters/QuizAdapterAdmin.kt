package com.example.quizapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Models.QuizMain
import com.example.quizapp.R

class QuizAdapterAdmin(
    private val quizList: MutableList<QuizMain>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<QuizAdapterAdmin.QuizViewHolder>() {

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.name_quiz_avaible_admin)
        val textViewDesc: TextView = itemView.findViewById(R.id.desc_quiz_avaible_admin)
        val editButton: ImageButton = itemView.findViewById(R.id.edit_btn)
        val deleteButton: ImageButton = itemView.findViewById(R.id.delete_btn)

        init {
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val quiz = quizList[position]
                    listener.onEditClick(quiz.id!!, quiz.name!!, quiz.description!!)
                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val quizId = quizList[position].id!!
                    listener.onDeleteClick(quizId)
                }
            }
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val id = quizList[position].id!!
                    val name = quizList[position].name!!
                    val desc = quizList[position].description!!
                    listener.onItemClick(id, name, desc)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quiz_rv_admin, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quizItem = quizList[position]
        holder.textView.text = quizItem.name
        holder.textViewDesc.text = quizItem.description
    }

    override fun getItemCount(): Int = quizList.size
}
