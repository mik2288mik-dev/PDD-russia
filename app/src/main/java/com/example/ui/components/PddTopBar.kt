package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PddCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PddTopBar(
    title: String,
    currentCategory: PddCategory,
    showBackButton: Boolean,
    onBackClick: () -> Unit,
    onCategorySelect: (PddCategory) -> Unit
) {
    var showCategoryMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = com.example.ui.theme.VibrantBackground,
            titleContentColor = com.example.ui.theme.VibrantOnBackground
        ),
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад"
                    )
                }
            }
        },
        actions = {
            // Category Toggle Pill (ABM / CD)
            Box {
                Row(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(com.example.ui.theme.VibrantBlueContainer)
                        .clickable { showCategoryMenu = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentCategory == PddCategory.ABM) Icons.Default.DirectionsCar else Icons.Default.LocalShipping,
                        contentDescription = null,
                        tint = com.example.ui.theme.VibrantOnBlueContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentCategory.code,
                        color = com.example.ui.theme.VibrantOnBlueContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    PddCategory.entries.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = category.title, fontWeight = FontWeight.Bold)
                                    Text(text = category.description, fontSize = 12.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onCategorySelect(category)
                                showCategoryMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (category == PddCategory.ABM) Icons.Default.DirectionsCar else Icons.Default.LocalShipping,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}
