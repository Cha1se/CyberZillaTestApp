package com.cha1se.cyberzillatestapp.data

import android.content.Context
import com.cha1se.cyberzillatestapp.R
import com.cha1se.cyberzillatestapp.data.models.Event
import com.cha1se.cyberzillatestapp.data.models.Events
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import retrofit2.http.GET
import java.io.BufferedReader
import java.io.InputStreamReader

interface NetworkApi {
    @GET("/")
    fun getEventsList(context: Context): Response<Events>
}