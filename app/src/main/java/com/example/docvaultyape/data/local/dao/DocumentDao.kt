package com.example.docvaultyape.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.docvaultyape.data.local.entity.AccessLogEntity
import com.example.docvaultyape.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE type = :type ORDER BY createdAt DESC")
    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccessLog(log: AccessLogEntity)

    @Query("SELECT * FROM access_logs WHERE documentId = :documentId ORDER BY accessedAt DESC")
    fun getAccessLogs(documentId: String): Flow<List<AccessLogEntity>>
}
