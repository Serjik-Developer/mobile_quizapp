package com.example.quizapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.quizapp.Interfaces.OnItemClickListener
import com.example.quizapp.Models.QuizMain
import com.example.quizapp.R

class QuizAdapter(private val quizList: MutableList<QuizMain>, private val listener: OnItemClickListener) : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    inner class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.name_quiz_avaible)
        val textViewDesc: TextView = itemView.findViewById(R.id.desc_quiz_avaible)
        init {
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.quiz_rv, parent, false)
        return QuizViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quizItem = quizList[position]
        holder.textView.text = quizItem.name
        holder.textViewDesc.text = quizItem.description
    }

    override fun getItemCount(): Int = quizList.size
}