package com.example.quizapp.Interfaces

interface OnItemClickListenerAnswers {
    fun onDeleteClick(aid: String)
    fun onEditClick(aid: String, text: String, explanation:String, trueQustion:String)
}