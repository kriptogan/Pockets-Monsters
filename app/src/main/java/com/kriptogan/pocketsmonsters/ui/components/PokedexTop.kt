package com.kriptogan.pocketsmonsters.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.kriptogan.pocketsmonsters.R

@Composable
fun PokedexTop(
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.top_pokedex),
        contentDescription = "Top Pokedex Border",
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp), // Fixed height for consistent scaling
        contentScale = ContentScale.FillBounds
    )
}
