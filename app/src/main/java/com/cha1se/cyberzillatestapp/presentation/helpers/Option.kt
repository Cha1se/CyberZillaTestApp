package com.cha1se.cyberzillatestapp.presentation.helpers

sealed class Option(var name: String, var isAscending: Boolean = false) {
    object Nothing: Option("nothing")
    object Category: Option("category")
    object Alphabet: Option("alphabet")
    object Distance: Option("distance")
    object Date: Option("date")

//    fun changeIsAscending(option: Option, isAscending: Boolean) {  }
}