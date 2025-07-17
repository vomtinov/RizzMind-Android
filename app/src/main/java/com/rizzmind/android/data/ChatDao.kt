package com.rizzmind.android.data

import androidx.room.*

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_memory WHERE personIdentifier = :personId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessages(personId: String, limit: Int = 200): List<ChatMessage>
    
    @Query("SELECT * FROM chat_memory WHERE personIdentifier = :personId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLastMessages(personId: String, limit: Int = 10): List<ChatMessage>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
    
    @Query("DELETE FROM chat_memory WHERE personIdentifier = :personId AND id NOT IN (SELECT id FROM chat_memory WHERE personIdentifier = :personId ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun trimOldMessages(personId: String, keepCount: Int = 200)
    
    @Query("SELECT DISTINCT personIdentifier FROM chat_memory ORDER BY timestamp DESC")
    suspend fun getAllPersonIdentifiers(): List<String>
    
    @Query("DELETE FROM chat_memory WHERE personIdentifier = :personId")
    suspend fun clearMessagesForPerson(personId: String)
}