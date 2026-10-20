package com.phan.bit240007_foodmanager.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.phan.bit240007_foodmanager.data.model.Dish
import com.phan.bit240007_foodmanager.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MockApiService(private val context: Context) {
    private val gson = Gson()

    /**
     * Resolves the full resource endpoint smartly:
     * Handles cases where user enters:
     * - https://xyz.mockapi.io/api/v1
     * - https://xyz.mockapi.io/api/v1/
     * - https://xyz.mockapi.io/api/v1/BIT240007_Dish
     * - https://xyz.mockapi.io/api/v1/Dish
     */
    fun getResourceUrl(): String {
        val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
        var url = prefs.getString(Constants.PREF_KEY_MOCKAPI, Constants.DEFAULT_MOCKAPI_URL)?.trim() ?: Constants.DEFAULT_MOCKAPI_URL
        while (url.endsWith("/")) {
            url = url.substring(0, url.length - 1)
        }

        // If the URL already ends with a resource segment, keep it
        val segments = url.split("/")
        val lastSegment = segments.lastOrNull()?.lowercase() ?: ""
        if (lastSegment.contains("dish") || lastSegment.contains("food") || lastSegment == Constants.TABLE_DISH.lowercase()) {
            return url
        }

        return "$url/${Constants.TABLE_DISH}"
    }

    suspend fun testConnection(): Result<String> = withContext(Dispatchers.IO) {
        val endpoint = getResourceUrl()
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                Result.success("HTTP $responseCode: Kết nối thành công!")
            } else {
                val errorStream = conn.errorStream
                val errorMsg = errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP $responseCode: $errorMsg (Endpoint: $endpoint)"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("${e.javaClass.simpleName}: ${e.message} (Endpoint: $endpoint)"))
        }
    }

    suspend fun getRemoteDishes(): Result<List<Dish>> = withContext(Dispatchers.IO) {
        val endpoint = getResourceUrl()
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                val listType = object : TypeToken<List<Dish>>() {}.type
                val dishes: List<Dish> = gson.fromJson(response, listType) ?: emptyList()
                Result.success(dishes)
            } else {
                Result.failure(Exception("HTTP GET Error: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRemoteDish(dish: Dish): Result<Dish> = withContext(Dispatchers.IO) {
        val endpoint = getResourceUrl()
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(gson.toJson(dish))
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                val created: Dish = gson.fromJson(response, Dish::class.java)
                Result.success(created)
            } else {
                val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP POST $responseCode: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRemoteDish(dish: Dish): Result<Dish> = withContext(Dispatchers.IO) {
        val endpoint = getResourceUrl() + "/" + dish.id
        try {
            val url = URL(endpoint)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(gson.toJson(dish))
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = reader.readText()
                reader.close()
                val updated: Dish = gson.fromJson(response, Dish::class.java)
                Result.success(updated)
            } else {
                val errorMsg = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP PUT $responseCode: $errorMsg"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
