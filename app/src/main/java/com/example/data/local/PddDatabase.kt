package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.entity.ExamHistoryEntity
import com.example.data.entity.QuestionEntity
import com.example.data.entity.TrafficSignEntity
import com.example.data.entity.UserProgressEntity

@Database(
    entities = [
        UserProgressEntity::class,
        ExamHistoryEntity::class,
        QuestionEntity::class,
        TrafficSignEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class PddDatabase : RoomDatabase() {
    abstract fun pddDao(): PddDao

    companion object {
        @Volatile
        private var INSTANCE: PddDatabase? = null

        fun getDatabase(context: Context): PddDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PddDatabase::class.java,
                    "pdd_database_2026.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
