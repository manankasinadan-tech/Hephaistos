package com.example.ipc

import android.content.Context
import android.content.Intent
import com.example.model.HephaistosIpcConstants
import com.example.model.IpcRequest
import com.example.model.IpcResponse
import com.example.model.TerminalEntry
import com.example.model.TerminalEntryType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.UUID

object HephaistosIpcManager {

    private val _responses = MutableSharedFlow<IpcResponse>(extraBufferCapacity = 64)
    val responses: SharedFlow<IpcResponse> = _responses.asSharedFlow()

    private val _terminalEntries = MutableStateFlow<List<TerminalEntry>>(
        listOf(
            TerminalEntry(
                id = "init-1",
                type = TerminalEntryType.INFO,
                text = "Hephaistos Terminal v1.0.0 [IPC Orchestrator]\nTapez 'help' pour la liste des commandes ou sélectionnez une cible."
            )
        )
    )
    val terminalEntries: StateFlow<List<TerminalEntry>> = _terminalEntries.asStateFlow()

    fun log(text: String, type: TerminalEntryType) {
        val entry = TerminalEntry(
            id = UUID.randomUUID().toString(),
            type = type,
            text = text
        )
        _terminalEntries.value = _terminalEntries.value + entry
    }

    fun clearLog() {
        _terminalEntries.value = listOf(
            TerminalEntry(
                id = UUID.randomUUID().toString(),
                type = TerminalEntryType.INFO,
                text = "Console réinitialisée. Prêt pour les commandes IPC."
            )
        )
    }

    fun sendCommand(
        context: Context,
        targetPackage: String,
        command: String,
        args: Map<String, Any?> = emptyMap()
    ): String {
        val reqId = "req-" + System.currentTimeMillis()
        val request = IpcRequest(
            id = reqId,
            source = context.packageName,
            target = targetPackage,
            command = command,
            args = args
        )

        val jsonPayload = JSONObject().apply {
            put("id", request.id)
            put("source", request.source)
            put("target", request.target)
            put("command", request.command)
            put("args", JSONObject(request.args))
            put("timestamp", System.currentTimeMillis())
        }.toString(2)

        log(">> [$targetPackage] $command\n$jsonPayload", TerminalEntryType.INPUT)

        try {
            val intent = Intent(HephaistosIpcConstants.ACTION_COMMAND).apply {
                `package` = targetPackage
                putExtra(HephaistosIpcConstants.EXTRA_PAYLOAD_JSON, jsonPayload)
                putExtra(HephaistosIpcConstants.EXTRA_REQUEST_ID, reqId)
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            }
            context.sendBroadcast(intent)
            log("Commande émise via Broadcast Intent -> $targetPackage", TerminalEntryType.INFO)
        } catch (e: Exception) {
            log("Erreur d'émission IPC: ${e.message}", TerminalEntryType.ERROR)
        }

        return reqId
    }

    fun onResponseReceived(jsonString: String) {
        try {
            val json = JSONObject(jsonString)
            val response = IpcResponse(
                id = json.optString("id", "unknown"),
                source = json.optString("source", "unknown"),
                target = json.optString("target", HephaistosIpcConstants.SOURCE_LAUNCHER),
                status = json.optString("status", "unknown"),
                code = json.optInt("code", 0),
                data = json.optJSONObject("data")?.let { obj ->
                    val map = mutableMapOf<String, Any?>()
                    obj.keys().forEach { k -> map[k] = obj.get(k) }
                    map
                },
                error = if (json.has("error") && !json.isNull("error")) json.optString("error") else null,
                timestamp = json.optLong("timestamp", System.currentTimeMillis())
            )

            val prettyJson = json.toString(2)
            val logType = if (response.status == "error" || response.code != 0) {
                TerminalEntryType.ERROR
            } else {
                TerminalEntryType.OUTPUT
            }

            log("<< [${response.source}] (code ${response.code}):\n$prettyJson", logType)
            _responses.tryEmit(response)
        } catch (e: Exception) {
            log("<< Réponse brute reçue (erreur de parsing JSON):\n$jsonString", TerminalEntryType.OUTPUT)
        }
    }

    fun executeCliString(context: Context, rawLine: String, activeEcosystemPackages: List<String>) {
        val trimmed = rawLine.trim()
        if (trimmed.isEmpty()) return

        when {
            trimmed.equals("help", ignoreCase = true) -> {
                val help = """
Commandes disponibles :
  help                                  Affiche ce guide d'aide
  clear                                 Efface l'historique de la console
  list                                  Liste les modules de l'écosystème actifs
  ping <package>                        Envoie une commande 'ping' au module cible
  send <package> <command> [json_args]  Envoie une commande personnalisée
  demo                                  Simule une réponse locale pour tester l'IPC

Exemples :
  ping com.hephaistos.unpacker
  send com.hephaistos.unpacker extract_partition {"partition":"system"}
                """.trimIndent()
                log(help, TerminalEntryType.INFO)
            }
            trimmed.equals("clear", ignoreCase = true) -> {
                clearLog()
            }
            trimmed.equals("list", ignoreCase = true) -> {
                if (activeEcosystemPackages.isEmpty()) {
                    log("Aucun module Hephaistos actuellement détecté. Activez des applications dans Paramètres > Applications de l'environnement.", TerminalEntryType.INFO)
                } else {
                    val sb = StringBuilder("Modules actifs de l'environnement :\n")
                    activeEcosystemPackages.forEach { sb.append("  • $it\n") }
                    log(sb.toString().trimEnd(), TerminalEntryType.INFO)
                }
            }
            trimmed.startsWith("ping ", ignoreCase = true) -> {
                val target = trimmed.substring(5).trim()
                sendCommand(context, target, "ping", emptyMap())
            }
            trimmed.startsWith("send ", ignoreCase = true) -> {
                val parts = trimmed.substring(5).trim().split(" ", limit = 3)
                if (parts.size < 2) {
                    log("Syntaxe: send <package> <command> [json_args]", TerminalEntryType.ERROR)
                    return
                }
                val target = parts[0]
                val cmd = parts[1]
                val argsMap = mutableMapOf<String, Any?>()
                if (parts.size >= 3) {
                    try {
                        val json = JSONObject(parts[2])
                        json.keys().forEach { k -> argsMap[k] = json.get(k) }
                    } catch (e: Exception) {
                        log("Erreur dans les arguments JSON: ${e.message}", TerminalEntryType.ERROR)
                        return
                    }
                }
                sendCommand(context, target, cmd, argsMap)
            }
            trimmed.equals("demo", ignoreCase = true) -> {
                log("Simulation d'une commande 'inspect_image' vers com.hephaistos.unpacker...", TerminalEntryType.INFO)
                val dummyResponse = JSONObject().apply {
                    put("id", "req-demo-" + System.currentTimeMillis())
                    put("source", "com.hephaistos.unpacker")
                    put("target", HephaistosIpcConstants.SOURCE_LAUNCHER)
                    put("status", "success")
                    put("code", 0)
                    put("data", JSONObject().apply {
                        put("image", "system.img")
                        put("filesystem", "erofs")
                        put("sparse", true)
                        put("size_bytes", 2841920000L)
                    })
                    put("error", JSONObject.NULL)
                    put("timestamp", System.currentTimeMillis())
                }.toString(2)
                onResponseReceived(dummyResponse)
            }
            else -> {
                // If typed directly like "com.hephaistos.unpacker ping"
                val tokens = trimmed.split(" ", limit = 3)
                if (tokens.size >= 2 && tokens[0].contains(".")) {
                    val target = tokens[0]
                    val cmd = tokens[1]
                    val argsMap = mutableMapOf<String, Any?>()
                    if (tokens.size >= 3) {
                        try {
                            val json = JSONObject(tokens[2])
                            json.keys().forEach { k -> argsMap[k] = json.get(k) }
                        } catch (_: Exception) {
                            argsMap["raw"] = tokens[2]
                        }
                    }
                    sendCommand(context, target, cmd, argsMap)
                } else {
                    log("Commande inconnue : '$trimmed'. Tapez 'help' pour les instructions.", TerminalEntryType.ERROR)
                }
            }
        }
    }
}
