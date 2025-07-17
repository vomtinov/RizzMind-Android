package com.rizzmind.android.memory

import android.content.Context
import com.rizzmind.android.data.ChatDatabase
import com.rizzmind.android.data.ChatMessage
import com.rizzmind.android.utils.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryManager(private val context: Context) {
    private val database = ChatDatabase.getDatabase(context)
    private val chatDao = database.chatDao()
    
    suspend fun storeMessage(
        sender: String,
        message: String,
        personIdentifier: String,
        timestamp: Long = TimeUtil.getCurrentTimestamp()
    ) = withContext(Dispatchers.IO) {
        val chatMessage = ChatMessage(
            timestamp = timestamp,
            sender = sender,
            message = message,
            personIdentifier = personIdentifier
        )
        
        chatDao.insertMessage(chatMessage)
        chatDao.trimOldMessages(personIdentifier, 200)
    }
    
    suspend fun getRecentConversation(
        personIdentifier: String,
        messageCount: Int = 10
    ): List<ChatMessage> = withContext(Dispatchers.IO) {
        chatDao.getLastMessages(personIdentifier, messageCount).reversed()
    }
    
    suspend fun formatConversationForGPT(
        personIdentifier: String,
        messageCount: Int = 10
    ): String = withContext(Dispatchers.IO) {
        val messages = getRecentConversation(personIdentifier, messageCount)
        
        if (messages.isEmpty()) {
            return@withContext "No recent conversation history available."
        }
        
        val formattedMessages = messages.map { message ->
            val senderDisplay = if (message.sender == "me") "Me" else message.sender
            val timeString = TimeUtil.formatTime(message.timestamp)
            "[$senderDisplay $timeString]: ${message.message}"
        }
        
        "Chat so far (timestamped):\n${formattedMessages.joinToString("\n")}"
    }
    
    suspend fun hasRecentActivity(personIdentifier: String, minutesThreshold: Int = 30): Boolean =
        withContext(Dispatchers.IO) {
            val recentMessages = chatDao.getLastMessages(personIdentifier, 1)
            recentMessages.isNotEmpty() && 
                    TimeUtil.isWithinLastMinutes(recentMessages.first().timestamp, minutesThreshold)
        }
    
    suspend fun getAllConversationPeople(): List<String> = withContext(Dispatchers.IO) {
        chatDao.getAllPersonIdentifiers()
    }
    
    suspend fun clearConversation(personIdentifier: String) = withContext(Dispatchers.IO) {
        chatDao.clearMessagesForPerson(personIdentifier)
    }
}