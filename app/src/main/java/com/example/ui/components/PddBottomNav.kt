package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.VibrantBlueContainer
import com.example.ui.theme.VibrantOnBackground
import com.example.ui.theme.VibrantOnBlueContainer
import com.example.ui.viewmodel.ScreenType

@Composable
fun PddBottomNav(
    currentScreen: ScreenType,
    onNavigate: (ScreenType) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show bottom nav on main tabs
    if (currentScreen == ScreenType.QUIZ) return

    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        contentColor = VibrantOnBackground
    ) {
        NavigationBarItem(
            selected = currentScreen == ScreenType.HOME,
            onClick = { onNavigate(ScreenType.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Главная") },
            label = { Text("Главная", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VibrantBlueContainer,
                selectedIconColor = VibrantOnBlueContainer,
                selectedTextColor = VibrantOnBlueContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.TICKET_LIST,
            onClick = { onNavigate(ScreenType.TICKET_LIST) },
            icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "Билеты") },
            label = { Text("Билеты", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VibrantBlueContainer,
                selectedIconColor = VibrantOnBlueContainer,
                selectedTextColor = VibrantOnBlueContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.TOPIC_LIST,
            onClick = { onNavigate(ScreenType.TOPIC_LIST) },
            icon = { Icon(Icons.Default.Category, contentDescription = "Темы") },
            label = { Text("Темы", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VibrantBlueContainer,
                selectedIconColor = VibrantOnBlueContainer,
                selectedTextColor = VibrantOnBlueContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.HANDBOOK,
            onClick = { onNavigate(ScreenType.HANDBOOK) },
            icon = { Icon(Icons.Default.Book, contentDescription = "ПДД и Знаки") },
            label = { Text("ПДД 2026", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VibrantBlueContainer,
                selectedIconColor = VibrantOnBlueContainer,
                selectedTextColor = VibrantOnBlueContainer
            )
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.STATS,
            onClick = { onNavigate(ScreenType.STATS) },
            icon = { Icon(Icons.Default.BarChart, contentDescription = "Статистика") },
            label = { Text("Успехи", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = VibrantBlueContainer,
                selectedIconColor = VibrantOnBlueContainer,
                selectedTextColor = VibrantOnBlueContainer
            )
        )
    }
}

