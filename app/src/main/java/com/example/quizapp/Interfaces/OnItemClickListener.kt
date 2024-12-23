package com.example.quizapp.Interfaces

interface OnItemClickListener {
    fun onItemClick(quizId: String, quizName:String, quizDesc: String)
    fun onDeleteClick(quizId: String)
    fun onEditClick(quizId: String, quizName: String, quizDesc: String)
}