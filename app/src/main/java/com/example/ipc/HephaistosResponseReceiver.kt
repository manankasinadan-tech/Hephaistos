package com.example.ipc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.model.HephaistosIpcConstants

class HephaistosResponseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return
        if (intent.action == HephaistosIpcConstants.ACTION_RESPONSE) {
            val payload = intent.getStringExtra(HephaistosIpcConstants.EXTRA_PAYLOAD_JSON)
            if (!payload.isNullOrBlank()) {
                HephaistosIpcManager.onResponseReceived(payload)
            } else {
                // If sent as raw extra
                val source = intent.getStringExtra("source") ?: "inconnu"
                val status = intent.getStringExtra("status") ?: "success"
                val raw = "{\"source\":\"$source\",\"status\":\"$status\",\"note\":\"Payload textuel sans JSON structuré\"}"
                HephaistosIpcManager.onResponseReceived(raw)
            }
        }
    }
}
