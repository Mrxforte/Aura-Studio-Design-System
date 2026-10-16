package com.example.data.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiStudioService {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    fun hasValidApiKey(): Boolean {
        val key = getApiKey()
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Aura conversational creative assistant request.
     * Maps user natural language requests to creative suggestions, tips, and editing tools.
     */
    suspend fun chatWithAura(
        prompt: String,
        history: List<Pair<String, Boolean>> = emptyList() // Pair(text, isUser)
    ): Result<AuraResponse> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent local creative intelligence fallback
            return@withContext Result.success(
                generateLocalAuraResponse(prompt)
            )
        }

        try {
            val systemPrompt = """
                You are Aura, an elite creative AI director and senior photo/video editor inside Aura Studio (a professional creative editing suite inspired by Photoshop and Picsart).
                Assist the user with creative styling, prompt improvements, color grading tips, and image editing suggestions.
                Keep responses concise, inspiring, and professional.
                Also suggest relevant tool tags in square brackets like [TOOL:ADJUST], [TOOL:FILTER], [TOOL:BG_REMOVE], [TOOL:ERASE], [TOOL:VIDEO], [TOOL:LOGO], or [TOOL:COLLAGE].
            """.trimIndent()

            val contentsArray = JSONArray()

            // System turn / context
            for ((text, isUser) in history.takeLast(6)) {
                val turn = JSONObject()
                turn.put("role", if (isUser) "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                turn.put("parts", parts)
                contentsArray.put(turn)
            }

            // Current prompt
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentTurn.put("parts", currentParts)
            contentsArray.put(currentTurn)

            val bodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                })
            }

            val request = Request.Builder()
                .url("${BASE_URL}gemini-3.5-flash:generateContent?key=$apiKey")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val rawBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini error (${response.code}): $rawBody"))
            }

            val json = JSONObject(rawBody)
            val candidates = json.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val replyText = parts?.optJSONObject(0)?.optString("text") ?: "Aura is ready to help."

            // Parse suggested tools
            val tools = mutableListOf<String>()
            if (replyText.contains("[TOOL:ADJUST]")) tools.add("Adjustments")
            if (replyText.contains("[TOOL:FILTER]")) tools.add("Filters")
            if (replyText.contains("[TOOL:BG_REMOVE]")) tools.add("Remove Background")
            if (replyText.contains("[TOOL:ERASE]")) tools.add("Object Eraser")
            if (replyText.contains("[TOOL:VIDEO]")) tools.add("Video Editor")
            if (replyText.contains("[TOOL:LOGO]")) tools.add("Logo Maker")
            if (replyText.contains("[TOOL:COLLAGE]")) tools.add("Collage Maker")

            val cleanText = replyText.replace(Regex("\\[TOOL:[A-Z_]+\\]"), "").trim()

            Result.success(AuraResponse(cleanText, tools))
        } catch (e: Exception) {
            Result.success(generateLocalAuraResponse(prompt))
        }
    }

    /**
     * Generates an image using Gemini 2.5 Flash Image REST API.
     */
    suspend fun generateImage(
        prompt: String,
        aspectRatio: String = "1:1",
        stylePreset: String = "Photorealistic"
    ): Result<Bitmap?> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                Exception("Gemini API key is not configured in Secrets panel. Using studio procedural engine.")
            )
        }

        try {
            val enhancedPrompt = "$prompt, in $stylePreset visual style, masterpiece quality, 8k resolution, cinematic lighting, sharp details"

            val bodyJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", enhancedPrompt))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("TEXT")
                        put("IMAGE")
                    })
                    put("imageConfig", JSONObject().apply {
                        put("aspectRatio", aspectRatio)
                        put("imageSize", "1K")
                    })
                })
            }

            val request = Request.Builder()
                .url("${BASE_URL}gemini-2.5-flash-image:generateContent?key=$apiKey")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val raw = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("API Error (${response.code}): $raw"))
            }

            val json = JSONObject(raw)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val parts = candidate?.optJSONObject("content")?.optJSONArray("parts")

            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val p = parts.getJSONObject(i)
                    val inlineData = p.optJSONObject("inlineData")
                    if (inlineData != null) {
                        val base64Data = inlineData.optString("data")
                        if (base64Data.isNotEmpty()) {
                            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            return@withContext Result.success(bmp)
                        }
                    }
                }
            }

            Result.failure(Exception("No image returned from Gemini."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateLocalAuraResponse(prompt: String): AuraResponse {
        val lower = prompt.lowercase()
        return when {
            lower.contains("background") || lower.contains("remove bg") -> AuraResponse(
                "I can instantly separate the foreground from your photo and preserve full transparency. Tap 'Remove Background' below to open our precision segmentation tool.",
                listOf("Remove Background", "Adjustments")
            )
            lower.contains("erase") || lower.contains("object") || lower.contains("clean") -> AuraResponse(
                "Use our Object Eraser! Simply paint over the unwanted object or blemish, and our synthesis engine will content-aware fill the region seamlessly.",
                listOf("Object Eraser", "Retouch")
            )
            lower.contains("cinematic") || lower.contains("filter") || lower.contains("color") -> AuraResponse(
                "For a cinematic look, I recommend increasing Contrast (+25), lowering Saturation slightly (-10), adding a slight Warmth (+15), and applying our 'Cinematic' or 'Film Noir' filter with subtle Vignette.",
                listOf("Filters", "Adjustments")
            )
            lower.contains("video") || lower.contains("clip") || lower.contains("timeline") -> AuraResponse(
                "Let's craft your video timeline! Aura Video Editor supports multi-track trimming, speed ramping (0.5x to 2x), aspect ratios (9:16 vertical & 16:9), and text overlays.",
                listOf("Video Editor", "Templates")
            )
            lower.contains("logo") || lower.contains("brand") -> AuraResponse(
                "Let's design a distinctive brand identity! Head over to the Logo Maker to select an emblem, typography, geometric badges, and export a clean vector PNG.",
                listOf("Logo Maker", "Templates")
            )
            lower.contains("collage") || lower.contains("grid") -> AuraResponse(
                "The Collage Maker offers customizable multi-slot grids with adjustable margins, rounded corners, and custom background colors. Perfect for story sharing!",
                listOf("Collage Maker", "Templates")
            )
            else -> AuraResponse(
                "I've analyzed your creative brief: \"$prompt\". Here are top recommendations: enhance exposure balance, apply our Cyberpunk or Golden Hour filter, or generate custom AI graphic layers.",
                listOf("Adjustments", "Filters", "AI Image Generator")
            )
        }
    }
}

data class AuraResponse(
    val message: String,
    val suggestedTools: List<String>
)
