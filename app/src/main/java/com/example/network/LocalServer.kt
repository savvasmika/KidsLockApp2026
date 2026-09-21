package com.example.network

import android.util.Log
import com.example.security.CryptoUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket

class LocalServer(
    private val port: Int = 8765,
    private val onPairProposalReceived: (parentDeviceId: String, parentName: String, code: String) -> Unit,
    private val onUnlockCommandReceived: (parentDeviceId: String) -> Boolean,
    private val onLockCommandReceived: (parentDeviceId: String) -> Boolean,
    private val onTemporaryCodeVerify: (code: String) -> Boolean,
    private val getStatusJson: () -> JSONObject
) {
    private val tag = "KidLockServer"
    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    var currentPort: Int = port
        private set

    fun start() {
        if (serverJob != null) return
        serverJob = scope.launch {
            try {
                // Try configured port, fallback to port 0 (dynamic available) if occupied
                serverSocket = try {
                    ServerSocket(port)
                } catch (e: Exception) {
                    Log.w(tag, "Port $port occupied, trying dynamic port", e)
                    ServerSocket(0)
                }
                currentPort = serverSocket?.localPort ?: port
                Log.d(tag, "LocalServer running on port $currentPort")

                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    launch {
                        handleClient(socket)
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(tag, "LocalServer exception", e)
                }
            }
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 10000
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val out = socket.getOutputStream()

            // Read HTTP request line
            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            if (parts.size < 2) return
            val method = parts[0]
            val path = parts[1]

            // Read headers to determine Content-Length
            var contentLength = 0
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                if (line.isNullOrEmpty()) break
                if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
                    contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            // Read body
            val body = if (contentLength > 0) {
                val chars = CharArray(contentLength)
                var readTotal = 0
                while (readTotal < contentLength) {
                    val r = reader.read(chars, readTotal, contentLength - readTotal)
                    if (r == -1) break
                    readTotal += r
                }
                String(chars, 0, readTotal)
            } else ""

            handleRequest(method, path, body, out)
        } catch (e: Exception) {
            Log.e(tag, "Error handling client", e)
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun handleRequest(method: String, path: String, body: String, out: OutputStream) {
        val jsonBody = try {
            if (body.isNotBlank()) JSONObject(body) else JSONObject()
        } catch (_: Exception) {
            JSONObject()
        }

        when {
            path == "/status" && method == "GET" -> {
                val status = getStatusJson()
                sendResponse(out, 200, status.toString())
            }
            path == "/pair/propose" && method == "POST" -> {
                val parentDeviceId = jsonBody.optString("parentDeviceId")
                val parentName = jsonBody.optString("parentName", "Parent Device")
                val code = jsonBody.optString("code")
                onPairProposalReceived(parentDeviceId, parentName, code)
                val response = JSONObject().apply {
                    put("status", "received")
                    put("message", "Confirmation displayed on child device")
                }
                sendResponse(out, 200, response.toString())
            }
            path == "/unlock" && method == "POST" -> {
                val parentDeviceId = jsonBody.optString("parentDeviceId")
                val success = onUnlockCommandReceived(parentDeviceId)
                val response = JSONObject().apply {
                    put("success", success)
                    put("message", if (success) "Unlocked successfully" else "Authentication failed")
                }
                sendResponse(out, if (success) 200 else 401, response.toString())
            }
            path == "/lock" && method == "POST" -> {
                val parentDeviceId = jsonBody.optString("parentDeviceId")
                val success = onLockCommandReceived(parentDeviceId)
                val response = JSONObject().apply {
                    put("success", success)
                    put("message", if (success) "Locked successfully" else "Authentication failed")
                }
                sendResponse(out, if (success) 200 else 401, response.toString())
            }
            path == "/code/verify" && method == "POST" -> {
                val code = jsonBody.optString("code")
                val valid = onTemporaryCodeVerify(code)
                val response = JSONObject().apply {
                    put("valid", valid)
                    put("message", if (valid) "Code accepted" else "Code invalid or expired")
                }
                sendResponse(out, if (valid) 200 else 400, response.toString())
            }
            else -> {
                sendResponse(out, 404, "{\"error\": \"Not Found\"}")
            }
        }
    }

    private fun sendResponse(out: OutputStream, statusCode: Int, body: String) {
        val statusText = when (statusCode) {
            200 -> "OK"
            400 -> "Bad Request"
            401 -> "Unauthorized"
            404 -> "Not Found"
            else -> "Error"
        }
        val bytes = body.toByteArray(Charsets.UTF_8)
        val header = "HTTP/1.1 $statusCode $statusText\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Content-Length: ${bytes.size}\r\n" +
                "Connection: close\r\n\r\n"
        out.write(header.toByteArray(Charsets.UTF_8))
        out.write(bytes)
        out.flush()
    }

    fun stop() {
        try {
            serverJob?.cancel()
            serverJob = null
            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            Log.e(tag, "Error stopping LocalServer", e)
        }
    }
}
