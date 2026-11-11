# Spécification Officielle de l'Écosystème HEPHAISTOS
> **Document de Référence pour Développeurs & Agents IA (Prompting & Auto-génération)**

Ce document définit les règles architecturales impératives permettant à n'importe quel développeur ou modèle d'intelligence artificielle de créer une nouvelle application constituant un **moteur puissant et interopérable** au sein de l'écosystème Hephaistos.

---

## 1. Vision & Architecture Fondatrice

L'écosystème Hephaistos est conçu pour le portage automatisé d'images système Android (Generic System Images - GSI), de partitions (`system`, `vendor`, `product`, `odm`) et de firmwares décompressés.

Au lieu d'une application monolithique lourde et instable, chaque brique est un module Android indépendant dédié à une tâche spécifique (ex: décompression sparse, conversion erofs, modification SELinux, signature dm-verity).

Toutes les applications partagent :
1. **La même convention de nommage**.
2. **Le même protocole de contrôle inter-processus (IPC)** via Broadcast Intents et JSON.
3. **Le même contrat de documentation téléchargeable (`manifest.json` ou `README.md`)**.

---

## 2. Convention de Nommage des Packages (OBLIGATOIRE)

Chaque application doit obligatoirement avoir comme identifiant de package :
```
com.hephaistos.<nom_application>
```

### Table des Modules Prévus & Réservés :
| Application / Module | Nom de Package | Rôle & Responsabilité |
| :--- | :--- | :--- |
| **Hephaistos Launcher** (Hôte) | `com.hephaistos.launcher` | Lanceur épuré, Dock macOS M3, Terminal de commande IPC, Orchestrateur |
| **GSI Unpacker** | `com.hephaistos.unpacker` | Décompression d'images sparse (`simg2img`), payload.bin, super.img |
| **Partition Editor** | `com.hephaistos.partmod` | Montage, redimensionnement ext4/erofs, injection de binaires |
| **Vendor Patcher** | `com.hephaistos.vendorpatch` | Résolution de dépendances HAL, superposition d'overlays, audio/caméra |
| **Apex & Treble Tool** | `com.hephaistos.treble` | Vérification de conformité Treble, extraction d'APEX |
| **Image Signer & Flasher** | `com.hephaistos.signer` | Signature AVB 2.0 / vbmeta, génération de scripts de flash Fastboot |

---

## 3. Contrat d'Interface Inter-Applications (IPC)

Les modules communiquent via des `BroadcastReceivers` standard sans nécessiter de permissions dangereuses ni de liaison RPC complexe.

### A. Format du Broadcast de Commande (Requête)
- **Action Intent** : `com.hephaistos.action.COMMAND`
- **Package cible** : `com.hephaistos.<nom_application>`
- **Extra `hephaistos_payload`** : Chaîne JSON contenant la commande
- **Extra `hephaistos_request_id`** : Identifiant unique de requête

#### Schéma JSON de Requête
```json
{
  "id": "req-1794407000-001",
  "source": "com.hephaistos.launcher",
  "target": "com.hephaistos.unpacker",
  "command": "extract_partition",
  "args": {
    "source_file": "/sdcard/Download/system.img",
    "partition": "system",
    "format": "raw"
  },
  "timestamp": 1794407000000
}
```

### B. Format du Broadcast de Réponse (Résultat)
- **Action Intent** : `com.hephaistos.action.RESPONSE`
- **Package cible** : `source` (ex: `com.hephaistos.launcher` ou autre module demandeur)
- **Extra `hephaistos_payload`** : Chaîne JSON contenant le résultat
- **Extra `hephaistos_request_id`** : Même identifiant que la requête

#### Schéma JSON de Réponse
```json
{
  "id": "req-1794407000-001",
  "source": "com.hephaistos.unpacker",
  "target": "com.hephaistos.launcher",
  "status": "success",
  "code": 0,
  "data": {
    "output_file": "/sdcard/Download/extracted/system.raw",
    "size_bytes": 3221225472,
    "sha256": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
  },
  "error": null,
  "timestamp": 1794407005000
}
```

---

## 4. Fichier `manifest.json` à Inclure dans Chaque Application

Chaque application doit placer un fichier `manifest.json` dans ses `assets/manifest.json` ou à la racine de son dépôt. Ce fichier est parsé par Hephaistos pour auto-découvrir les commandes disponibles.

```json
{
  "name": "Nom du Module",
  "package": "com.hephaistos.<module>",
  "version": "1.0.0",
  "description": "Description concise du moteur",
  "ipc": {
    "action_command": "com.hephaistos.action.COMMAND",
    "action_response": "com.hephaistos.action.RESPONSE",
    "commands": [
      {
        "name": "ping",
        "description": "Vérifie l'état du module",
        "args": {},
        "returns": { "status": "string", "uptime_ms": "number" }
      },
      {
        "name": "nom_commande",
        "description": "Description de l'action",
        "args": {
          "param_1": "type (ex: string, chemin d'accès)",
          "param_2": "type (ex: boolean, optionnel)"
        },
        "returns": {
          "result_key": "type de la valeur de retour"
        }
      }
    ]
  }
}
```

---

## 5. Modèle Prêt à l'Emploi pour une Nouvelle Application (Kotlin)

### AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.hephaistos.mon_module">

    <application ...>
        <!-- Récepteur des commandes d'Hephaistos et des autres modules -->
        <receiver
            android:name=".ipc.EcosystemCommandReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="com.hephaistos.action.COMMAND" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

### EcosystemCommandReceiver.kt
```kotlin
package com.hephaistos.mon_module.ipc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.json.JSONObject

class EcosystemCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.hephaistos.action.COMMAND") {
            val payloadString = intent.getStringExtra("hephaistos_payload") ?: return
            val requestJson = JSONObject(payloadString)
            val reqId = requestJson.optString("id")
            val source = requestJson.optString("source")
            val command = requestJson.optString("command")
            val args = requestJson.optJSONObject("args") ?: JSONObject()

            // Traitement de la commande
            val result = executeOperation(command, args)

            // Réponse formatée
            val responseJson = JSONObject().apply {
                put("id", reqId)
                put("source", "com.hephaistos.mon_module")
                put("target", source)
                put("status", if (result.isSuccess) "success" else "error")
                put("code", if (result.isSuccess) 0 else 1)
                put("data", result.dataJson)
                put("error", result.errorMessage)
                put("timestamp", System.currentTimeMillis())
            }

            // Émission de la réponse vers la source
            val responseIntent = Intent("com.hephaistos.action.RESPONSE").apply {
                `package` = source
                putExtra("hephaistos_payload", responseJson.toString())
                putExtra("hephaistos_request_id", reqId)
            }
            context.sendBroadcast(responseIntent)
        }
    }
}
```

---

## 6. Prompt Système Recommandé pour Guider une IA

Lorsqu'une IA doit générer une nouvelle application de l'écosystème, fournissez-lui cette consigne :

> "Tu dois créer une application Android autonome sous le package com.hephaistos.<nom>. Elle doit être pilotable sans interface obligatoirement affichée via un BroadcastReceiver écoutant com.hephaistos.action.COMMAND avec payload JSON. Elle doit respecter le protocole défini dans la spécification Hephaistos (ECOSYSTEM_SPEC.md) et inclure son manifest.json décrivant toutes ses commandes et arguments."
