package com.cha1se.cyberzillatestapp.presentation.helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

sealed class Resource<T>(
    var data: T? = null,
    var message: String? = null,
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Error<T>(message: String) : Resource<T>(message = message)
    class Loading<T> : Resource<T>()

}

suspend fun <T> safeApiCall(apiToBeCalled: suspend () -> Response<T>): Resource<T> {

    return withContext(Dispatchers.IO) {
        try {
            val response: Response<T> = apiToBeCalled()
            if (response.isSuccessful) {
                Resource.Success(data = response.body()!!)
            } else {
                println("----ERROR----")
                Resource.Error(response.message())
            }

        } catch (e: HttpException) {
            println("----ERROR----")
            Resource.Error(message = e.message ?: "Something went wrong")
        } catch (e: IOException) {
            println("----ERROR----")
            Resource.Error("Please check your network connection")
        } catch (e: Exception) {
            println("----ERROR----")
            Resource.Error(message = e.message.toString())
        }
    }
}
