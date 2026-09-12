package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TrafficSign

@Entity(tableName = "traffic_signs")
data class TrafficSignEntity(
    @PrimaryKey val code: String,
    val title: String,
    val group: String,
    val description: String,
    val iconType: String
) {
    fun toTrafficSign(): TrafficSign = TrafficSign(
        code = code,
        title = title,
        group = group,
        description = description,
        iconType = iconType
    )
}

fun TrafficSign.toEntity(): TrafficSignEntity = TrafficSignEntity(
    code = code,
    title = title,
    group = group,
    description = description,
    iconType = iconType
)
