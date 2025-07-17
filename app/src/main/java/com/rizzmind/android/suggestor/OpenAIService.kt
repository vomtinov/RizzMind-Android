package com.rizzmind.android.suggestor

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class OpenAIRequest(
    val model: String = "gpt-4",
    val messages: List<OpenAIMessage>,
    val max_tokens: Int = 200,
    val temperature: Double = 0.8,
    val n: Int = 1
)

data class OpenAIMessage(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val choices: List<OpenAIChoice>
)

data class OpenAIChoice(
    val message: OpenAIMessage
)

interface OpenAIService {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenAIRequest
    ): OpenAIResponse
}