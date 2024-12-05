package com.cha1se.cyberzillatestapp.di

import com.cha1se.cyberzillatestapp.data.EventsRepositoryImpl
import com.cha1se.cyberzillatestapp.data.NetworkApi
import com.cha1se.cyberzillatestapp.presentation.MainActivity
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

val dataModule = module {
    val baseUrl = "http://google.com"

    single<NetworkApi> {

        val cacheSize = 10 * 1024 * 1024 // 10 MB cache
        val cache = Cache(androidContext().cacheDir, cacheSize.toLong())

        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (MainActivity().hasNetwork(androidContext()))
                    request.newBuilder().header("Cache-Control", "public, max-age=60").build()
                else
                    request.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                        .build()
                chain.proceed(request)
            }
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build().create(NetworkApi::class.java)
    }
    single { EventsRepositoryImpl(networkApi = get(), context = get()) }

}