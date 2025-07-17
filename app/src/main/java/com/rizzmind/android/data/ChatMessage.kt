package com.rizzmind.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_memory")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val sender: String, // "me" or contact name/identifier
    val message: String,
    val personIdentifier: String // To group conversations by person
)