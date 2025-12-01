package com.kriptogan.pocketsmonsters.data.tcg

import com.google.gson.annotations.SerializedName

/**
 * Represents a Pokémon TCG card
 */
data class TCGCard(
    val id: String,
    val name: String,
    @SerializedName("supertype")
    val supertype: String? = null,
    @SerializedName("subtypes")
    val subtypes: List<String>? = null,
    @SerializedName("level")
    val level: String? = null,
    @SerializedName("hp")
    val hp: String? = null,
    @SerializedName("types")
    val types: List<String>? = null,
    @SerializedName("evolvesFrom")
    val evolvesFrom: String? = null,
    @SerializedName("abilities")
    val abilities: List<TCGCardAbility>? = null,
    @SerializedName("attacks")
    val attacks: List<TCGCardAttack>? = null,
    @SerializedName("weaknesses")
    val weaknesses: List<TCGCardWeakness>? = null,
    @SerializedName("resistances")
    val resistances: List<TCGCardResistance>? = null,
    @SerializedName("retreatCost")
    val retreatCost: List<String>? = null,
    @SerializedName("convertedRetreatCost")
    val convertedRetreatCost: Int? = null,
    @SerializedName("set")
    val set: TCGCardSet? = null,
    @SerializedName("number")
    val number: String? = null,
    @SerializedName("artist")
    val artist: String? = null,
    @SerializedName("rarity")
    val rarity: String? = null,
    @SerializedName("flavorText")
    val flavorText: String? = null,
    @SerializedName("nationalPokedexNumbers")
    val nationalPokedexNumbers: List<Int>? = null,
    @SerializedName("legalities")
    val legalities: Map<String, String>? = null,
    @SerializedName("regulationMark")
    val regulationMark: String? = null,
    @SerializedName("images")
    val images: TCGCardImages? = null,
    @SerializedName("tcgplayer")
    val tcgplayer: TCGCardTcgPlayer? = null
)

/**
 * TCG Card ability
 */
data class TCGCardAbility(
    val name: String? = null,
    val text: String? = null,
    val type: String? = null
)

/**
 * TCG Card attack
 */
data class TCGCardAttack(
    val name: String? = null,
    val cost: List<String>? = null,
    @SerializedName("convertedEnergyCost")
    val convertedEnergyCost: Int? = null,
    val damage: String? = null,
    val text: String? = null
)

/**
 * TCG Card weakness
 */
data class TCGCardWeakness(
    val type: String? = null,
    val value: String? = null
)

/**
 * TCG Card resistance
 */
data class TCGCardResistance(
    val type: String? = null,
    val value: String? = null
)

/**
 * TCG Card set information
 */
data class TCGCardSet(
    val id: String? = null,
    val name: String? = null,
    val series: String? = null,
    @SerializedName("printedTotal")
    val printedTotal: Int? = null,
    val total: Int? = null,
    @SerializedName("legalities")
    val legalities: Map<String, String>? = null,
    @SerializedName("ptcgoCode")
    val ptcgoCode: String? = null,
    @SerializedName("releaseDate")
    val releaseDate: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("images")
    val images: TCGSetImages? = null
)

/**
 * TCG Card images
 */
data class TCGCardImages(
    @SerializedName("small")
    val small: String? = null,
    @SerializedName("large")
    val large: String? = null
)

/**
 * TCG Player information
 */
data class TCGCardTcgPlayer(
    val url: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    val prices: TCGCardPrices? = null
)

/**
 * TCG Card prices
 */
data class TCGCardPrices(
    val normal: TCGCardPrice? = null,
    val holofoil: TCGCardPrice? = null,
    @SerializedName("reverseHolofoil")
    val reverseHolofoil: TCGCardPrice? = null
)

/**
 * TCG Card price
 */
data class TCGCardPrice(
    val low: Double? = null,
    val mid: Double? = null,
    val high: Double? = null,
    val market: Double? = null,
    @SerializedName("directLow")
    val directLow: Double? = null
)

