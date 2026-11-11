package com.example.data

import android.content.Context
import android.content.Intent

object EcosystemDocs {

    val MANIFEST_JSON_SCHEMA: String = """
{
  "${'$'}schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "HephaistosEcosystemManifest",
  "description": "Spécification standard pour les applications et moteurs constitutifs de l'écosystème Hephaistos.",
  "type": "object",
  "required": ["name", "package", "version", "description", "ipc"],
  "properties": {
    "name": {
      "type": "string",
      "example": "Hephaistos GSI Unpacker"
    },
    "package": {
      "type": "string",
      "pattern": "^com\\.hephaistos\\.[a-z0-9_]+${'$'}",
      "example": "com.hephaistos.unpacker"
    },
    "version": {
      "type": "string",
      "example": "1.0.0"
    },
    "description": {
      "type": "string",
      "example": "Moteur d'extraction et de conversion de partitions système Android (sparse / raw / payload.bin)."
    },
    "ipc": {
      "type": "object",
      "required": ["action_command", "action_response", "commands"],
      "properties": {
        "action_command": {
          "type": "string",
          "default": "com.hephaistos.action.COMMAND"
        },
        "action_response": {
          "type": "string",
          "default": "com.hephaistos.action.RESPONSE"
        },
        "commands": {
          "type": "array",
          "items": {
            "type": "object",
            "required": ["name", "description", "args", "returns"],
            "properties": {
              "name": { "type": "string" },
              "description": { "type": "string" },
              "args": {
                "type": "object",
                "additionalProperties": { "type": "string" }
              },
              "returns": {
                "type": "object",
                "additionalProperties": { "type": "string" }
              }
            }
          }
        }
      }
    }
  }
}
""".trimIndent()

    val SAMPLE_MANIFEST_JSON: String = """
{
  "name": "Hephaistos GSI Unpacker",
  "package": "com.hephaistos.unpacker",
  "version": "1.0.0",
  "description": "Module de décompression et conversion d'images système (sparse/raw/payload.bin/erofs)",
  "author": "Hephaistos Ecosystem",
  "ipc": {
    "action_command": "com.hephaistos.action.COMMAND",
    "action_response": "com.hephaistos.action.RESPONSE",
    "commands": [
      {
        "name": "ping",
        "description": "Vérifie la disponibilité du module et renvoie son statut",
        "args": {},
        "returns": {
          "status": "string (ex: ready)",
          "uptime_ms": "number"
        }
      },
      {
        "name": "extract_partition",
        "description": "Extrait une partition spécifique depuis une image source",
        "args": {
          "source_image": "string (chemin absolu du fichier source)",
          "partition": "string (ex: system, vendor, product)",
          "output_dir": "string (dossier de destination optionnel)"
        },
        "returns": {
          "output_path": "string",
          "file_size_bytes": "number",
          "checksum_sha256": "string"
        }
      },
      {
        "name": "inspect_image",
        "description": "Analyse les métadonnées et le type de système de fichiers d'une image",
        "args": {
          "source_image": "string"
        },
        "returns": {
          "filesystem": "string (ext4 | erofs | f2fs)",
          "is_sparse": "boolean",
          "size_bytes": "number"
        }
      }
    ]
  }
}
""".trimIndent()

    val COMPLETE_README_MD: String = """
# Spécification Standard de l'Écosystème HEPHAISTOS

## 1. Philosophie & Règle d'Or
L'écosystème **Hephaistos** est une suite modulaire d'outils et de moteurs Android conçus pour le portage automatique de Generic System Images (GSI), de partitions système (`system`, `vendor`, `product`, `system_ext`) et d'images unpackées.

Chaque application de l'écosystème est :
1. **Un moteur autonome** : Capable d'exécuter des opérations spécialisées sans dépendre de bibliothèques tierces propriétaires.
2. **Pilotable à distance** : Contrôlable par le lanceur Hephaistos, par d'autres modules de l'écosystème, ou par un orchestrateur IA automatisé.
3. **Auto-documentée** : Chaque module embarque un fichier `manifest.json` ou `README.md` téléchargeable décrivant ses commandes, arguments d'entrée et format de sortie JSON.

---

## 2. Convention de Nommage Impérative
Toutes les applications de l'écosystème **DOIVENT** respecter le préfixe de package :
```
com.hephaistos.<nom_application>
```
Exemples :
- Lanceur / Hôte : `com.hephaistos.launcher`
- Décompresseur d'images : `com.hephaistos.unpacker`
- Outil de patch GSI : `com.hephaistos.gsipatch`
- Modificateur de partitions : `com.hephaistos.partmod`
- Signeur de package OTA : `com.hephaistos.signer`

---

## 3. Protocole de Communication Inter-Applications (IPC)

Les communications s'effectuent via des Broadcast Intents Android asynchrones transportant des payloads JSON standardisés.

### A. Action de Commande (Requête)
- **Action Intent** : `com.hephaistos.action.COMMAND`
- **Package cible** : `com.hephaistos.<nom_module>`
- **Extra JSON** : `hephaistos_payload`
- **Extra ID** : `hephaistos_request_id`

#### Format de Requête JSON
```json
{
  "id": "req-20261111-001",
  "source": "com.hephaistos.launcher",
  "target": "com.hephaistos.unpacker",
  "command": "extract_partition",
  "args": {
    "source_image": "/sdcard/Download/system.img",
    "partition": "system"
  }
}
```

### B. Action de Réponse (Résultat)
- **Action Intent** : `com.hephaistos.action.RESPONSE`
- **Package cible** : `source` (ex: `com.hephaistos.launcher`)
- **Extra JSON** : `hephaistos_payload`
- **Extra ID** : `hephaistos_request_id`

#### Format de Réponse JSON
```json
{
  "id": "req-20261111-001",
  "source": "com.hephaistos.unpacker",
  "target": "com.hephaistos.launcher",
  "status": "success",
  "code": 0,
  "data": {
    "output_path": "/sdcard/Download/extracted/system.raw.img",
    "size_bytes": 3145728000
  },
  "error": null,
  "timestamp": 1794407000000
}
```

---

## 4. Guide d'Implémentation dans un Nouveau Module (Kotlin)

Pour qu'une nouvelle application soit 100% compatible avec Hephaistos :

### Étape 1 : Déclaration dans AndroidManifest.xml
```xml
<receiver
    android:name=".receiver.HephaistosCommandReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="com.hephaistos.action.COMMAND" />
    </intent-filter>
</receiver>
```

### Étape 2 : Réception et Exécution
```kotlin
package com.hephaistos.unpacker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONObject

class HephaistosCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.hephaistos.action.COMMAND") {
            val payload = intent.getStringExtra("hephaistos_payload") ?: return
            val json = JSONObject(payload)
            val reqId = json.optString("id")
            val source = json.optString("source")
            val cmd = json.optString("command")
            val args = json.optJSONObject("args")

            // Exécution de la commande
            val response = executeCommand(cmd, args)

            // Envoi de la réponse
            val responseIntent = Intent("com.hephaistos.action.RESPONSE").apply {
                `package` = source
                putExtra("hephaistos_payload", response.toString())
                putExtra("hephaistos_request_id", reqId)
            }
            context.sendBroadcast(responseIntent)
        }
    }
}
```

---

## 5. Intégration GitHub Actions & CI
Chaque projet peut être compilé et packagé directement avec le workflow standard :
`.github/workflows/android.yml`
""".trimIndent()

    fun shareText(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        val chooser = Intent.createChooser(intent, title)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
