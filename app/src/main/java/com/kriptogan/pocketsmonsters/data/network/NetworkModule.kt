package com.kriptogan.pocketsmonsters.data.network

import android.content.Context
import com.kriptogan.pocketsmonsters.data.api.PokeApiService
import com.kriptogan.pocketsmonsters.data.local.LocalStorage
import com.kriptogan.pocketsmonsters.data.offline.OfflineDataLoader
import com.kriptogan.pocketsmonsters.data.repository.PokemonRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private const val BASE_URL = "https://pokeapi.co/api/v2/"
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    fun createPokeApiService(): PokeApiService {
        return retrofit.create(PokeApiService::class.java)
    }
    
    fun createOfflineDataLoader(context: Context): OfflineDataLoader {
        return OfflineDataLoader(context)
    }
    
    fun createPokemonRepository(context: Context): PokemonRepository {
        val localStorage = LocalStorage(context)
        val offlineDataLoader = createOfflineDataLoader(context)
        return PokemonRepository(localStorage, offlineDataLoader)
    }
}
