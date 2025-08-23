package com.kriptogan.pocketsmonsters.data.models

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val stats: List<Stat>,
    val types: List<TypeSlot>,
    val sprites: Sprites,
    @SerializedName("base_experience")
    val baseExperience: Int
)
