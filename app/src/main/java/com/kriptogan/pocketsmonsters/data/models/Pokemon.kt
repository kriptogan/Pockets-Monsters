package com.kriptogan.pocketsmonsters.data.models

import com.google.gson.annotations.SerializedName

data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val stats: List<Stat>,
    val types: List<TypeSlot>,
    @SerializedName("base_experience")
    val baseExperience: Int,
    val abilities: List<AbilitySlot>,
    @SerializedName("level_up_moves")
    val levelUpMoves: List<LevelUpMove>,
    @SerializedName("sprite_path")
    val spritePath: String
)

data class LevelUpMove(
    val name: String,
    @SerializedName("level_learned_at")
    val levelLearnedAt: Int,
    @SerializedName("version_group")
    val versionGroup: String
)

data class AbilitySlot(
    val ability: Ability,
    @SerializedName("is_hidden")
    val isHidden: Boolean,
    val slot: Int
)

data class Ability(
    val name: String
)
