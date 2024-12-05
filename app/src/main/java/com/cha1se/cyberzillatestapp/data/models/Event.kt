package com.cha1se.cyberzillatestapp.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable


data class Events(
    var list: List<Event>
)

@Serializable
data class Event(
    val name: String,
    val description: String,
    val date: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
)
