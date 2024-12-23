package com.example.quizapp.Interfaces

interface OnItemClickListenerQuestions {
    fun onItemClick(qid: String, question:String)
    fun onDeleteClick(qid: String)
    fun onEditClick(qid: String, question: String)
}