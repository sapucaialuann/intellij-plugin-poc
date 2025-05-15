package com.github.sapucaialuann.intellijpluginpoc.services

import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray

object GPTService {
    private const val API_KEY = "your_key"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    fun callGpt(prompt: String): String {
        val client = OkHttpClient()

        val part = JSONObject().put("text", prompt)
        val parts = JSONArray().put(part)

        val content = JSONObject().put("parts", parts)
        val contents = JSONArray().put(content)

        val bodyJson = JSONObject().put("contents", contents)

        println("Sending to Gemini:\n${bodyJson.toString(2)}")

        val mediaType = "application/json".toMediaType()
        val requestBody = RequestBody.create(mediaType, bodyJson.toString())

        val request = Request.Builder()
            .url("$API_URL?key=$API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->

            println("Response:\n${response}")
            val responseBody = response.body?.string() ?: return "No response"
            val json = JSONObject(responseBody)

            return json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()
        }
    }
}