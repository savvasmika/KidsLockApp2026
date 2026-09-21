package com.example.model

enum class MessageType {
    PAIR_PROPOSAL,
    PAIR_CONFIRM,
    STATUS_QUERY,
    STATUS_REPLY,
    UNLOCK_COMMAND,
    LOCK_COMMAND,
    REQUEST_UNLOCK,
    VERIFY_CODE
}

data class NetworkMessage(
    val type: MessageType,
    val senderDeviceId: String,
    val targetDeviceId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val payload: String = "",
    val signature: String = ""
)
