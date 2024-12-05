package com.cha1se.cyberzillatestapp.data

import android.content.Context
import com.cha1se.cyberzillatestapp.R
import com.cha1se.cyberzillatestapp.data.models.Event
import com.cha1se.cyberzillatestapp.data.models.Events
import com.cha1se.cyberzillatestapp.presentation.MainActivity
import com.cha1se.cyberzillatestapp.presentation.helpers.Resource
import com.cha1se.cyberzillatestapp.presentation.helpers.safeApiCall
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.internal.http2.ErrorCode
import retrofit2.Response
import java.io.BufferedReader
import java.io.InputStreamReader

class EventsRepositoryImpl(val networkApi: NetworkApi,val context: Context) {
    suspend fun getEventsList(): Flow<Resource<Events>> = flowOf(
        safeApiCall { Response.success(getDataFromJson(context)) }
    )

    fun getDataFromJson(context: Context): Events {
        val inputStream = context.resources.openRawResource(R.raw.data)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data: Events = Gson().fromJson(bufferedReader.readText(), Events::class.java)

        inputStream.close()
        return data
    }
}
