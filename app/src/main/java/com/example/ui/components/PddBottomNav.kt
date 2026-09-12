package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        NavigationBarItem(
            selected = currentScreen == ScreenType.HOME,
            onClick = { onNavigate(ScreenType.HOME) },
            icon = {
                Icon(
                    if (currentScreen == ScreenType.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Главная"
                )
            },
            label = { Text("Главная", style = MaterialTheme.typography.labelMedium) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.TICKET_LIST,
            onClick = { onNavigate(ScreenType.TICKET_LIST) },
            icon = {
                Icon(
                    if (currentScreen == ScreenType.TICKET_LIST) Icons.Filled.FormatListNumbered else Icons.Outlined.FormatListNumbered,
                    contentDescription = "Билеты"
                )
            },
            label = { Text("Билеты", style = MaterialTheme.typography.labelMedium) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.TOPIC_LIST,
            onClick = { onNavigate(ScreenType.TOPIC_LIST) },
            icon = {
                Icon(
                    if (currentScreen == ScreenType.TOPIC_LIST) Icons.Filled.Category else Icons.Outlined.Category,
                    contentDescription = "Темы"
                )
            },
            label = { Text("Темы", style = MaterialTheme.typography.labelMedium) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.HANDBOOK,
            onClick = { onNavigate(ScreenType.HANDBOOK) },
            icon = {
                Icon(
                    if (currentScreen == ScreenType.HANDBOOK) Icons.Filled.Book else Icons.Outlined.Book,
                    contentDescription = "ПДД и Знаки"
                )
            },
            label = { Text("ПДД 2026", style = MaterialTheme.typography.labelMedium) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentScreen == ScreenType.STATS,
            onClick = { onNavigate(ScreenType.STATS) },
            icon = {
                Icon(
                    if (currentScreen == ScreenType.STATS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Статистика"
                )
            },
            label = { Text("Успехи", style = MaterialTheme.typography.labelMedium) },
            colors = itemColors
        )
    }
}


