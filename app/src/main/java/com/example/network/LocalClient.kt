package com.example.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LocalClient {
    private val tag = "KidLockClient"

    suspend fun sendPairProposal(
        host: String,
        port: Int,
        parentDeviceId: String,
        parentName: String,
        code: String
    ): Result<JSONObject> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("parentDeviceId", parentDeviceId)
            put("parentName", parentName)
            put("code", code)
        }
        postJson("http://$host:$port/pair/propose", payload)
    }

    suspend fun getStatus(host: String, port: Int): Result<JSONObject> = withContext(Dispatchers.IO) {
        getJson("http://$host:$port/status")
    }

    suspend fun sendUnlockCommand(
        host: String,
        port: Int,
        parentDeviceId: String,
        secretKey: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("parentDeviceId", parentDeviceId)
        }
        postJson("http://$host:$port/unlock", payload).map {
            it.optBoolean("success", false)
        }
    }

    suspend fun sendLockCommand(
        host: String,
        port: Int,
        parentDeviceId: String,
        secretKey: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("parentDeviceId", parentDeviceId)
        }
        postJson("http://$host:$port/lock", payload).map {
            it.optBoolean("success", false)
        }
    }

    suspend fun verifyCode(host: String, port: Int, code: String): Result<Boolean> = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("code", code)
        }
        postJson("http://$host:$port/code/verify", payload).map {
            it.optBoolean("valid", false)
        }
    }

    private fun postJson(urlString: String, json: JSONObject): Result<JSONObject> {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.doOutput = true

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(json.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }

            if (responseCode in 200..299) {
                Result.success(if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject())
            } else {
                Result.failure(Exception("HTTP error $responseCode: $responseText"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to post to $urlString", e)
            Result.failure(e)
        }
    }

    private fun getJson(urlString: String): Result<JSONObject> {
        return try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            val responseCode = conn.responseCode
            val stream = if (responseCode in 200..299) conn.inputStream else conn.errorStream
            val responseText = BufferedReader(InputStreamReader(stream)).use { it.readText() }

            if (responseCode in 200..299) {
                Result.success(if (responseText.isNotBlank()) JSONObject(responseText) else JSONObject())
            } else {
                Result.failure(Exception("HTTP error $responseCode: $responseText"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed GET to $urlString", e)
            Result.failure(e)
        }
    }
}
