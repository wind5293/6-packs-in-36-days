package com.example.fitflow.data

import android.util.Base64
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

object ChatbotApiService {

    private val client =
            OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .build()

    private const val API_KEY = "[ENCRYPTION_KEY]"
    private const val SYSTEM_PROMPT =
            """
        You are FitFlow's AI fitness coach. 
        You help users with workout advice, nutrition tips, recovery strategies, 
        and motivation. You can also analyze the calories and nutritional value of food.
        Keep responses concise and practical.
        Reply in the same language the user writes in.
    """

    suspend fun sendMessage(
            userMessage: String,
            history: List<Pair<String, Boolean>>, // text, isFromUser
            imageBytes: ByteArray? = null,
            imageMimeType: String? = null
    ): Result<String> =
            withContext(Dispatchers.IO) {
                try {
                    val contentsArray = JSONArray()
                    history.forEach { (text, isFromUser) ->
                        if (text.isNotBlank()) {
                            contentsArray.put(
                                    JSONObject().apply {
                                        put("role", if (isFromUser) "user" else "model")
                                        put(
                                                "parts",
                                                JSONArray()
                                                        .put(
                                                                JSONObject().apply {
                                                                    put("text", text)
                                                                }
                                                        )
                                        )
                                    }
                            )
                        }
                    }
                    contentsArray.put(
                            JSONObject().apply {
                                put("role", "user")
                                val partsArray = JSONArray()
                                partsArray.put(
                                        JSONObject().apply {
                                            put(
                                                    "text",
                                                    if (userMessage.isBlank() && imageBytes != null)
                                                            "Phân tích lượng calo và dinh dưỡng của đồ ăn trong ảnh này."
                                                    else userMessage
                                            )
                                        }
                                )

                                if (imageBytes != null && imageMimeType != null) {
                                    val base64Data =
                                            Base64.encodeToString(imageBytes, Base64.NO_WRAP)
                                    partsArray.put(
                                            JSONObject().apply {
                                                put(
                                                        "inline_data",
                                                        JSONObject().apply {
                                                            put("mime_type", imageMimeType)
                                                            put("data", base64Data)
                                                        }
                                                )
                                            }
                                    )
                                }
                                put("parts", partsArray)
                            }
                    )

                    val body =
                            JSONObject().apply {
                                put(
                                        "systemInstruction",
                                        JSONObject().apply {
                                            put(
                                                    "parts",
                                                    JSONArray()
                                                            .put(
                                                                    JSONObject().apply {
                                                                        put(
                                                                                "text",
                                                                                SYSTEM_PROMPT
                                                                                        .trimIndent()
                                                                        )
                                                                    }
                                                            )
                                            )
                                        }
                                )
                                put("contents", contentsArray)
                            }

                    val request =
                            Request.Builder()
                                    .url(
                                            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${API_KEY}"
                                    )
                                    .addHeader("Content-Type", "application/json")
                                    .post(
                                            body.toString()
                                                    .toRequestBody("application/json".toMediaType())
                                    )
                                    .build()

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        val json = JSONObject(responseBody)
                        val text =
                                json.getJSONArray("candidates")
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
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
