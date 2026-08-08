package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.PddViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandbookScreen(viewModel: PddViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedGroupFilter by remember { mutableStateOf("Все") }

    val signs = viewModel.getTrafficSigns()
    val ruleSections = viewModel.getPddRuleSections()

    val groups = listOf("Все", "Предупреждающие", "Знаки приоритета", "Запрещающие", "Предписывающие", "Знаки особых предписаний", "Информационные")

    val filteredSigns = signs.filter { sign ->
        (selectedGroupFilter == "Все" || sign.group == selectedGroupFilter) &&
                (searchQuery.isEmpty() || sign.title.contains(searchQuery, ignoreCase = true) || sign.code.contains(searchQuery))
    }

    val filteredRules = ruleSections.filter { rule ->
        searchQuery.isEmpty() || rule.title.contains(searchQuery, ignoreCase = true) || rule.content.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Дорожные знаки", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Signpost, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Текст ПДД 2026", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
            )
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(if (selectedTab == 0) "Поиск знака по коду или названию..." else "Поиск в тексте ПДД...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Очистить")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        if (selectedTab == 0) {
            // Group Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                items(groups) { group ->
                    FilterChip(
                        selected = selectedGroupFilter == group,
                        onClick = { selectedGroupFilter = group },
                        label = { Text(group, fontSize = 12.sp) }
                    )
                }
            }

            // Signs List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSigns) { sign ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when (sign.group) {
                                            "Запрещающие" -> Color(0xFFFEE2E2)
                                            "Знаки приоритета" -> Color(0xFFFEF3C7)
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sign.code,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = when (sign.group) {
                                        "Запрещающие" -> Color(0xFFDC2626)
                                        "Знаки приоритета" -> Color(0xFFD97706)
                                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sign.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = sign.group,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sign.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Rules Sections List
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredRules) { section ->
                    var isExpanded by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = section.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = section.summary,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            AnimatedVisibility(visible = isExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = section.content,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 22.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
