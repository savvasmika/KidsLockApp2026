package com.example.model

enum class DeviceRole {
    UNSET,
    PARENT,
    CHILD
}

enum class LockState {
    LOCKED,
    UNLOCKED
}

enum class ConnectionState {
    CONNECTED_LOCAL,
    CONNECTED_REMOTE,
    CONNECTING,
    OFFLINE
}
