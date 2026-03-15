package com.example.docvaultyape.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "access_logs",
    foreignKeys = [ForeignKey(
        entity = DocumentEntity::class,
        parentColumns = ["id"],
        childColumns = ["documentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("documentId")]
)
data class AccessLogEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val accessedAt: Long,
    val action: String
)
