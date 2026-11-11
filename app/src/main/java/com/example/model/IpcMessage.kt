package com.example.model

object HephaistosIpcConstants {
    const val ACTION_COMMAND = "com.hephaistos.action.COMMAND"
    const val ACTION_RESPONSE = "com.hephaistos.action.RESPONSE"
    const val EXTRA_PAYLOAD_JSON = "hephaistos_payload"
    const val EXTRA_REQUEST_ID = "hephaistos_request_id"
    const val SOURCE_LAUNCHER = "com.hephaistos.launcher"
}

data class IpcRequest(
    val id: String,
    val source: String = HephaistosIpcConstants.SOURCE_LAUNCHER,
    val target: String,
    val command: String,
    val args: Map<String, Any?> = emptyMap()
)

data class IpcResponse(
    val id: String,
    val source: String,
    val target: String = HephaistosIpcConstants.SOURCE_LAUNCHER,
    val status: String, // "success", "error", "progress"
    val code: Int = 0,
    val data: Map<String, Any?>? = null,
    val error: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TerminalEntryType {
    INPUT,
    OUTPUT,
    ERROR,
    INFO
}

data class TerminalEntry(
    val id: String,
    val type: TerminalEntryType,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
