package com.rizzmind.android.suggestor

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.rizzmind.android.memory.MemoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.UnknownHostException

data class Suggestion(
    val text: String,
    val confidence: Float = 1.0f,
    val source: String = "AI"
)

class SuggestionEngine(private val context: Context) {
    private val memoryManager = MemoryManager(context)
    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    private val openAIService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIService::class.java)
    }
    
    suspend fun generateSuggestions(
        recentOcrText: String,
        personIdentifier: String,
        currentTimestamp: Long
    ): List<Suggestion> = withContext(Dispatchers.IO) {
        
        // Store the OCR text as a message from the other person
        if (recentOcrText.isNotBlank()) {
            val inferredSender = inferSenderFromText(recentOcrText, personIdentifier)
            if (inferredSender != "me") {
                memoryManager.storeMessage(
                    sender = inferredSender,
                    message = recentOcrText,
                    personIdentifier = personIdentifier,
                    timestamp = currentTimestamp
                )
            }
        }
        
        return@withContext try {
            when (getSuggestionMode()) {
                "gpt" -> generateGPTSuggestions(personIdentifier)
                "local" -> generateLocalSuggestions(recentOcrText)
                else -> generateFallbackSuggestions()
            }
        } catch (e: Exception) {
            generateFallbackSuggestions()
        }
    }
    
    private suspend fun generateGPTSuggestions(personIdentifier: String): List<Suggestion> {
        val apiKey = getOpenAIApiKey()
        if (apiKey.isBlank()) {
            return generateFallbackSuggestions()
        }
        
        val conversationHistory = memoryManager.formatConversationForGPT(personIdentifier)
        val suggestionCount = getSuggestionCount()
        
        val prompt = buildString {
            append(conversationHistory)
            append("\n\nSuggest $suggestionCount playful, confident, flirty replies that would be good responses to the last message.")
            append(" Each reply should be on a new line, numbered 1., 2., 3. etc.")
            append(" Keep responses natural, fun, and engaging. Avoid being overly formal.")
        }
        
        return try {
            val response = openAIService.getChatCompletion(
                authorization = "Bearer $apiKey",
                request = OpenAIRequest(
                    model = "gpt-4",
                    messages = listOf(
                        OpenAIMessage(
                            role = "system",
                            content = "You are a witty, charming assistant that helps generate natural, engaging text message replies. Generate exactly the requested number of suggestions, each on a new line with numbering."
                        ),
                        OpenAIMessage(
                            role = "user",
                            content = prompt
                        )
                    ),
                    max_tokens = 200,
                    temperature = 0.8
                )
            )
            
            parseGPTResponse(response.choices.firstOrNull()?.message?.content ?: "")
        } catch (e: ConnectException) {
            generateOfflineSuggestions("No internet connection")
        } catch (e: UnknownHostException) {
            generateOfflineSuggestions("Network unavailable")
        } catch (e: Exception) {
            generateFallbackSuggestions()
        }
    }
    
    private fun parseGPTResponse(response: String): List<Suggestion> {
        return response.split("\n")
            .filter { it.trim().isNotEmpty() }
            .mapNotNull { line ->
                val cleanLine = line.trim()
                    .removePrefix("1.")
                    .removePrefix("2.")
                    .removePrefix("3.")
                    .removePrefix("4.")
                    .removePrefix("5.")
                    .trim()
                    .removeSurrounding("\"")
                
                if (cleanLine.isNotBlank()) {
                    Suggestion(cleanLine, 0.9f, "GPT-4")
                } else null
            }
            .take(getSuggestionCount())
            .ifEmpty { generateFallbackSuggestions() }
    }
    
    private fun generateLocalSuggestions(recentText: String): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val count = getSuggestionCount()
        
        // Simple pattern-based suggestions
        val lowercaseText = recentText.lowercase()
        
        when {
            lowercaseText.contains("hey") || lowercaseText.contains("hi") -> {
                suggestions.addAll(listOf(
                    Suggestion("Hey there! 😊", 0.8f, "Local"),
                    Suggestion("What's up?", 0.7f, "Local"),
                    Suggestion("Hey! How's your day going?", 0.6f, "Local")
                ))
            }
            lowercaseText.contains("how are you") -> {
                suggestions.addAll(listOf(
                    Suggestion("I'm good! How about you?", 0.8f, "Local"),
                    Suggestion("Doing great, thanks for asking!", 0.7f, "Local"),
                    Suggestion("Can't complain! What about you?", 0.6f, "Local")
                ))
            }
            lowercaseText.contains("what") && lowercaseText.contains("up") -> {
                suggestions.addAll(listOf(
                    Suggestion("Just chilling! You?", 0.8f, "Local"),
                    Suggestion("Not much, what about you?", 0.7f, "Local"),
                    Suggestion("Just relaxing 😎", 0.6f, "Local")
                ))
            }
            lowercaseText.contains("😂") || lowercaseText.contains("haha") || lowercaseText.contains("lol") -> {
                suggestions.addAll(listOf(
                    Suggestion("😂😂", 0.8f, "Local"),
                    Suggestion("You're hilarious!", 0.7f, "Local"),
                    Suggestion("Right?? 😄", 0.6f, "Local")
                ))
            }
            else -> {
                suggestions.addAll(listOf(
                    Suggestion("That's interesting!", 0.6f, "Local"),
                    Suggestion("Tell me more", 0.5f, "Local"),
                    Suggestion("Sounds good!", 0.4f, "Local")
                ))
            }
        }
        
        return suggestions.take(count)
    }
    
    private fun generateFallbackSuggestions(): List<Suggestion> {
        val fallbackSuggestions = listOf(
            Suggestion("Sounds good!", 0.5f, "Fallback"),
            Suggestion("That's cool 😊", 0.5f, "Fallback"),
            Suggestion("Interesting!", 0.5f, "Fallback"),
            Suggestion("Tell me more", 0.4f, "Fallback"),
            Suggestion("Nice!", 0.4f, "Fallback")
        )
        
        return fallbackSuggestions.take(getSuggestionCount())
    }
    
    private fun generateOfflineSuggestions(reason: String): List<Suggestion> {
        return listOf(
            Suggestion("Sounds great!", 0.5f, "Offline"),
            Suggestion("Cool! 😊", 0.5f, "Offline"),
            Suggestion("That's awesome", 0.5f, "Offline")
        ).take(getSuggestionCount())
    }
    
    private fun inferSenderFromText(text: String, personIdentifier: String): String {
        // Simple heuristic: if text seems like a response pattern, it's probably from the other person
        // This is a basic implementation - in real use, you'd want more sophisticated detection
        return if (text.length > 5 && !text.startsWith("I ")) {
            personIdentifier
        } else {
            "me"
        }
    }
    
    private fun getSuggestionCount(): Int {
        return preferences.getInt("suggestion_count", 3).coerceIn(1, 5)
    }
    
    private fun getSuggestionMode(): String {
        return preferences.getString("suggestion_mode", "gpt") ?: "gpt"
    }
    
    private fun getOpenAIApiKey(): String {
        return preferences.getString("openai_api_key", "") ?: ""
    }
}