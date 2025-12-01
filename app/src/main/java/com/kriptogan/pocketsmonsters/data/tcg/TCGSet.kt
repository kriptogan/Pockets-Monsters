package com.kriptogan.pocketsmonsters.data.tcg

import com.google.gson.annotations.SerializedName

/**
 * Represents a Pokémon TCG set
 */
data class TCGSet(
    val id: String,
    val name: String,
    @SerializedName("series")
    val series: String? = null,
    @SerializedName("printedTotal")
    val printedTotal: Int? = null,
    @SerializedName("total")
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
 * TCG Set images
 */
data class TCGSetImages(
    @SerializedName("symbol")
    val symbol: String? = null,
    @SerializedName("logo")
    val logo: String? = null
)

