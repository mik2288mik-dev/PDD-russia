package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
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
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiConsultantScreen(viewModel: PddViewModel) {
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "AI",
                text = "Здравствуйте! Я вашего AI Эксперт ПДД 2026. Задайте мне любой вопрос о правилах дорожного движения, знаках, проезде перекрестков или спорных ситуациях на дороге!"
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("AI Консультант ПДД", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Экспертный советник по ПДД РФ 2026", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Chat Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.sender == "USER"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!isUser) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8B5CF6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Text(
                            text = msg.text,
                            fontSize = 14.sp,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp),
                            lineHeight = 20.sp
                        )
                    }

                    if (isUser) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Анализирует ПДД...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Input Field
        Surface(tonalElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Задайте вопрос по ПДД...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isThinking) {
                            val userMsg = inputText.trim()
                            messages.add(ChatMessage("USER", userMsg))
                            inputText = ""
                            isThinking = true

                            coroutineScope.launch {
                                kotlinx.coroutines.delay(1200)
                                val response = when {
                                    userMsg.contains("главн", ignoreCase = true) -> "Согласно п. 1.2 ПДД РФ, Главная дорога — это дорога, обозначенная знаками 2.1, 2.3.1-2.3.7 или 2.2, либо дорога с твердым покрытием по отношению к грунтовой."
                                    userMsg.contains("скорост", ignoreCase = true) -> "Максимальная скорость в населенных пунктах и жилых зонах — до 20 км/ч в жилых зонах и 60 км/ч на дорогах (если иное не установлено знаками)."
                                    userMsg.contains("обгон", ignoreCase = true) -> "Обгон запрещен на пешеходных переходах, ж/д переездах (и за 100 м до них), мостах, эстакадах, в тоннелях и в конце подъемов (п. 11.4 ПДД)."
                                    else -> "Согласно ПДД РФ 2026: водителю всегда необходимо уступать дорогу пешеходам на переходах, соблюдать требования знаков и разметки, а также безопасный боковой интервал и дистанцию."
                                }
                                messages.add(ChatMessage("AI", response))
                                isThinking = false
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    containerColor = Color(0xFF8B5CF6)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Отправить", tint = Color.White)
                }
            }
        }
    }
}
