package com.example.docvaultyape.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val encryptedFilePath: String,
    val thumbnailPath: String?,
    val sizeBytes: Long,
    val createdAt: Long,
    val locationAddress: String?,
    val latitude: Double?,
    val longitude: Double?
)
