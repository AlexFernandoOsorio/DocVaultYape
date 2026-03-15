package com.example.docvaultyape.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.docvaultyape.data.local.dao.DocumentDao
import com.example.docvaultyape.data.local.entity.AccessLogEntity
import com.example.docvaultyape.data.local.entity.DocumentEntity

@Database(
    entities = [DocumentEntity::class, AccessLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
}
