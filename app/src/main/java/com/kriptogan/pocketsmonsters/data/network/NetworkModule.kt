package com.kriptogan.pocketsmonsters.data.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kriptogan.pocketsmonsters.data.api.PokeApiService
import com.kriptogan.pocketsmonsters.data.local.LocalStorage
import com.kriptogan.pocketsmonsters.data.repository.PokemonRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private const val BASE_URL = "https://pokeapi.co/api/v2/"
    private const val TIMEOUT_SECONDS = 30L
    
    /**
     * Create and configure OkHttpClient with logging
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Create and configure Gson for JSON parsing
     */
    private fun createGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    /**
     * Create and configure Retrofit instance
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(createGson()))
            .build()
    }
    
    /**
     * Create PokeApiService instance
     */
    fun createPokeApiService(): PokeApiService {
        return createRetrofit().create(PokeApiService::class.java)
    }
    
    /**
     * Create LocalStorage instance
     */
    fun createLocalStorage(context: Context): LocalStorage {
        return LocalStorage(context)
    }
    
    /**
     * Create PokemonRepository instance
     */
    fun createPokemonRepository(context: Context): PokemonRepository {
        val localStorage = createLocalStorage(context)
        return PokemonRepository(localStorage)
    }
}
