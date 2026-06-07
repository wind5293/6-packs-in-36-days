package com.example.fitflow.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ClaudeApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private const val API_KEY = "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"
    private const val MODEL = "claude-sonnet-4-6"
    private const val SYSTEM_PROMPT = """
        You are FitFlow's AI fitness coach. 
        You help users with workout advice, nutrition tips, recovery strategies, 
        and motivation. Keep responses concise and practical.
        Reply in the same language the user writes in.
    """

    suspend fun sendMessage(
        userMessage: String,
        history: List<Pair<String, Boolean>> // text, isFromUser
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Build messages array từ history
            val messagesArray = JSONArray()
            history.forEach { (text, isFromUser) ->
                if (text.isNotBlank()) {
                    messagesArray.put(
                        JSONObject().apply {
                            put("role", if (isFromUser) "user" else "assistant")
                            put("content", text)
                        }
                    )
                }
            }
            // Thêm tin nhắn hiện tại
            messagesArray.put(
                JSONObject().apply {
                    put("role", "user")
                    put("content", userMessage)
                }
            )

            val body = JSONObject().apply {
                put("model", MODEL)
                put("max_tokens", 1024)
                put("system", SYSTEM_PROMPT.trimIndent())
                put("messages", messagesArray)
            }

            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", API_KEY)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("content-type", "application/json")
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val json = JSONObject(responseBody)
                val text = json
                    .getJSONArray("content")
                    .getJSONObject(0)
                    .getString("text")
                Result.success(text)
            } else {
                Result.failure(Exception("API error ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}