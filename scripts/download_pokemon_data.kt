#!/usr/bin/env kotlin

@file:DependsOn("com.squareup.okhttp3:okhttp:4.11.0")
@file:DependsOn("com.google.code.gson:gson:2.10.1")
@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Script to download all Pokémon data and save to assets directory
 * Run this script to populate the assets folder with offline data
 * 
 * Usage: kotlin scripts/download_pokemon_data.kt
 */

data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>
)

data class PokemonListItem(
    val name: String,
    val url: String
)

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val stats: List<Stat>,
    val types: List<TypeSlot>,
    val sprites: Sprites,
    val baseExperience: Int
)

data class Stat(
    val baseStat: Int,
    val effort: Int,
    val stat: StatInfo
)

data class StatInfo(
    val name: String,
    val url: String
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String,
    val url: String
)

data class Sprites(
    val frontDefault: String?,
    val frontShiny: String?,
    val frontFemale: String?,
    val frontShinyFemale: String?,
    val backDefault: String?,
    val backShiny: String?,
    val backFemale: String?,
    val backShinyFemale: String?
)

class PokemonDataDownloader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val baseUrl = "https://pokeapi.co/api/v2/"
    
    suspend fun downloadAllData(assetsDir: File) = withContext(Dispatchers.IO) {
        try {
            println("🚀 Starting Pokémon data download...")
            
            // Create assets directory if it doesn't exist
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
                println("📁 Created assets directory: ${assetsDir.absolutePath}")
            }
            
            // Step 1: Download basic list
            println("📋 Downloading Pokémon list...")
            val pokemonList = downloadPokemonList()
            val total = pokemonList.results.size
            println("✅ Found $total Pokémon to download")
            
            // Step 2: Download detailed data for each Pokémon
            val pokemonDetails = mutableListOf<Pokemon>()
            val spriteUrls = mutableMapOf<String, String>()
            
            pokemonList.results.forEachIndexed { index, pokemonItem ->
                try {
                    println("📥 Downloading ${pokemonItem.name} (${index + 1}/$total)")
                    
                    val pokemon = downloadPokemon(pokemonItem.name.lowercase())
                    pokemonDetails.add(pokemon)
                    
                    // Extract front sprite URL
                    val frontSpriteUrl = pokemon.sprites.frontDefault
                    if (frontSpriteUrl != null) {
                        spriteUrls[pokemon.name] = frontSpriteUrl
                    }
                    
                    // Small delay to be respectful to the API
                    delay(100)
                    
                } catch (e: Exception) {
                    println("❌ Failed to download ${pokemonItem.name}: ${e.message}")
                }
            }
            
            // Step 3: Save Pokémon data to JSON
            println("💾 Saving Pokémon data...")
            val pokemonDataFile = File(assetsDir, "pokemon_data.json")
            FileWriter(pokemonDataFile).use { writer ->
                gson.toJson(pokemonDetails, writer)
            }
            println("✅ Saved Pokémon data to: ${pokemonDataFile.absolutePath}")
            
            // Step 4: Save sprite URLs mapping
            println("🔗 Saving sprite URLs...")
            val spriteUrlsFile = File(assetsDir, "sprite_urls.json")
            FileWriter(spriteUrlsFile).use { writer ->
                gson.toJson(spriteUrls, writer)
            }
            println("✅ Saved sprite URLs to: ${spriteUrlsFile.absolutePath}")
            
            // Step 5: Download and save sprites
            println("🖼️ Downloading sprites...")
            val spritesDir = File(assetsDir, "sprites")
            if (!spritesDir.exists()) {
                spritesDir.mkdirs()
            }
            
            var successfulSprites = 0
            spriteUrls.forEach { (pokemonName, url) ->
                try {
                    downloadSprite(url, File(spritesDir, "${pokemonName.lowercase()}.png"))
                    successfulSprites++
                } catch (e: Exception) {
                    println("❌ Failed to download sprite for $pokemonName: ${e.message}")
                }
            }
            println("✅ Downloaded $successfulSprites sprites")
            
            // Step 6: Save metadata
            val timestamp = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(timestamp))
            
            val metadata = mapOf(
                "total_pokemon" to total,
                "successful_sprites" to successfulSprites,
                "download_timestamp" to timestamp,
                "download_date" to formattedDate,
                "version" to "1.0.0",
                "description" to "Offline Pokémon data for Pockets & Monsters app"
            )
            
            val metadataFile = File(assetsDir, "metadata.json")
            FileWriter(metadataFile).use { writer ->
                gson.toJson(metadata, writer)
            }
            println("✅ Saved metadata to: ${metadataFile.absolutePath}")
            
            // Summary
            println("\n🎉 Data download completed successfully!")
            println("📊 Summary:")
            println("   • Total Pokémon: $total")
            println("   • Sprites downloaded: $successfulSprites")
            println("   • Download date: $formattedDate")
            println("   • Assets directory: ${assetsDir.absolutePath}")
            println("\n📱 You can now build your app with offline data!")
            
        } catch (e: Exception) {
            println("💥 Data download failed: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    private suspend fun downloadPokemonList(): PokemonListResponse {
        val request = Request.Builder()
            .url("${baseUrl}pokemon?limit=1008")
            .build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to download Pokémon list: ${response.code}")
        }
        
        val json = response.body?.string() ?: throw Exception("Empty response")
        return gson.fromJson(json, PokemonListResponse::class.java)
    }
    
    private suspend fun downloadPokemon(name: String): Pokemon {
        val request = Request.Builder()
            .url("${baseUrl}pokemon/$name")
            .build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to download Pokémon $name: ${response.code}")
        }
        
        val json = response.body?.string() ?: throw Exception("Empty response for $name")
        return gson.fromJson(json, Pokemon::class.java)
    }
    
    private suspend fun downloadSprite(url: String, outputFile: File) {
        val request = Request.Builder()
            .url(url)
            .build()
        
        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            throw Exception("Failed to download sprite from $url: ${response.code}")
        }
        
        response.body?.byteStream()?.use { input ->
            outputFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

// Main execution
fun main() {
    val assetsDir = File("app/src/main/assets")
    
    runBlocking {
        val downloader = PokemonDataDownloader()
        downloader.downloadAllData(assetsDir)
    }
}
