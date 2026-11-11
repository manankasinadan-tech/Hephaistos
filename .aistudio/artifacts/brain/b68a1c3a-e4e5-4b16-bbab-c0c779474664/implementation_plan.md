# Hephaistos Launcher - Écosystème Modulaire Android

Un lanceur Android épuré et moderne conçu comme le chef d'orchestre de l'écosystème d'outils de portage GSI / partitions système (`com.hephaistos.*`), avec dock flottant inspiré de macOS, terminal de commande inter-modules (IPC JSON) et spécification ouverte téléchargeable.

## Décisions Clés & Retours Utilisateur

> [!IMPORTANT]
> Suite à vos retours lors de la phase de cadrage, voici les choix validés qui guideront l'architecture :

- **Communication Inter-Applications (Validée)** : Utilisation de `BroadcastReceiver` avec payloads JSON (`ACTION_HEPHAISTOS_COMMAND` et `ACTION_HEPHAISTOS_RESPONSE`) et extraits typés pour permettre à Hephaistos et à tous les futurs modules d'échanger des commandes sans dépendances directes.
- **Détection des Applications (Validée)** : Détection stricte et dynamique des véritables applications installées sous le préfixe `com.hephaistos.*`. Pour faciliter les tests et l'intégration avant le déploiement des futurs modules, Hephaistos dispose également d'un sélecteur manuel permettant d'inclure n'importe quelle application installée dans l'écosystème.
- **Documentation et Spécification (Validée)** : Visionneuse riche intégrée dans les Paramètres avec export en 1 clic au format JSON (`manifest.json`) et Markdown (`README.md`), ainsi qu'un modèle de workflow GitHub Actions pour compiler et packager l'écosystème.

---

## 1. Vue d'ensemble & Concept Fondateur

Hephaistos est la première pierre angulaire de l'écosystème. Contrairement à un lanceur généraliste encombré de widgets et d'applications tierces, Hephaistos est une interface dédiée aux développeurs et bidouilleurs de ROMs/GSI :
- **Rôle Hôte** : Regroupe, lance et pilote les modules spécialisés (ex: unpacker de partitions, modder vendor, convertisseur GSI, signeur d'images).
- **Interface Épurée** : Pas de surcharge visuelle ni de descriptions envahissantes. L'accent est mis sur la clarté et la fluidité.
- **Dock Flottant Style macOS** : Barre d'accès rapide inférieure avec effet de flou/surface élevée et 3 icônes Material 3 fondamentales :
  1. **Refresh** : Actualise immédiatement le catalogue des modules installés.
  2. **Console (Terminal)** : Terminal interactif pour envoyer des instructions JSON aux modules et visualiser leurs réponses brutes.
  3. **Paramètres** : Thème, sélection manuelle des modules et consultation/export de la documentation standard.

---

## 2. Expérience Utilisateur & Design Visuel

### A. Accueil & Grille d'Applications
- **Fond d'écran et Ambiance** : Design sobre et minimaliste avec des tons neutres sombres ou clairs et des accents discrets (style Titanium / Forgeron moderne).
- **Grille de Modules** : Cartes épurées avec icône de l'application et nom du module. Touch target de 48dp+ avec retour haptique et ripple Material 3.
- **État Vide Conscient** : Si aucun module `com.hephaistos.*` n'est détecté, un message élégant explique comment associer une application manuellement via les Paramètres ou envoyer des commandes test via la Console.

### B. Dock Flottant Inférieur (Style macOS M3)
- Barre flottante semi-transparente centrée horizontalement (`Modifier.navigationBarsPadding()`), coins arrondis généreux (`shape = RoundedCornerShape(24.dp)`).
- 3 Actions principales :
  - **Rafraîchir** : Animation de rotation lors de la détection de nouveaux packages.
  - **Console** : Ouvre le terminal en panneau relevable (bottom sheet ou vue dédiée) avec historique des commandes, autocomplétion des commandes connues et retour JSON formaté.
  - **Paramètres** : Ouvre les réglages du launcher.

### C. Terminal & Console Écosystème
- Interface type CLI avec prompt `hephaistos >`.
- Permet de taper des commandes ciblant une application (ex: `com.hephaistos.unpacker extract --input system.img` ou `broadcast send ...`).
- Hephaistos envoie le broadcast avec l'action et le payload JSON standardisé, puis affiche la réponse reçue de manière asynchrone dans le flux du terminal.

### D. Paramètres & Documentation
- **Sélecteur de Thème** : Système / Clair / Sombre avec bascule immédiate.
- **Gestionnaire d'Applications de l'Environnement** : Liste toutes les applications installées sur le périphérique avec interrupteurs pour forcer l'inclusion d'une app dans l'environnement Hephaistos.
- **Documentation Standardisée & Spécification IA** :
  - Spécification complète pour créer de futures applications satellites.
  - Boutons d'export directs pour enregistrer `manifest.json` et `README.md`.
  - Lien et instructions prêtes pour GitHub Actions.

---

## 3. Architecture Technique & Flux de Données

```
┌────────────────────────────────────────────────────────┐
│                   HEPHAISTOS LAUNCHER                  │
│               (com.hephaistos.launcher)                │
└──────────────────────────┬─────────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        ▼                  ▼                  ▼
┌──────────────┐   ┌──────────────┐   ┌──────────────┐
│  Home Grid   │   │  macOS Dock  │   │   Terminal   │
│ (Apps com.   │   │  [↻] [>_] [⚙]│   │ JSON Command │
│ hephaistos.*)│   └──────────────┘   │     IPC      │
└──────────────┘                      └───────┬──────┘
                                              │
                      Broadcast Intent / JSON │
                      ACTION_HEPHAISTOS_CMD   │
                                              ▼
                           ┌─────────────────────────────┐
                           │    Future Modules / Apps    │
                           │  - com.hephaistos.unpacker  │
                           │  - com.hephaistos.gsi_patch │
                           │  - com.hephaistos.signer    │
                           └─────────────────────────────┘
```

### Contrat d'Interface Standardisé (manifest.json)
Chaque application créée pour l'écosystème doit implémenter :
1. **Un BroadcastReceiver** écoutant `com.hephaistos.action.COMMAND`
2. **Format de Requête JSON** :
   ```json
   {
     "id": "req-12345",
     "source": "com.hephaistos.launcher",
     "target": "com.hephaistos.module_name",
     "command": "extract_partition",
     "args": {
       "image_path": "/sdcard/system.img",
       "partition": "system"
     }
   }
   ```
3. **Format de Réponse JSON** :
   ```json
   {
     "id": "req-12345",
     "status": "success",
     "code": 0,
     "data": {
       "output_directory": "/sdcard/extracted/system"
     },
     "error": null
   }
   ```

---

## 4. Étapes de Réalisation

1. **Configuration Gradle & Manifest** :
   - Mise à jour de `applicationId` / namespace vers `com.hephaistos.launcher`
   - Déclaration des permissions de requêtage de packages (`<queries>` pour `com.hephaistos.*` et `QUERY_ALL_PACKAGES` si nécessaire pour la sélection manuelle)
   - Synchronisation de `metadata.json` et `strings.xml`.
2. **Couche Données & PackageManager** :
   - Repository détectant les applications installées filtrées par préfixe `com.hephaistos.`
   - DataStore pour mémoriser les applications manuellement cochées par l'utilisateur et la préférence de thème.
3. **Moteur IPC & Terminal** :
   - `HephaistosIpcManager` gérant l'émission de Broadcasts JSON et l'écoute des réponses.
   - Terminal ViewModel gérant l'historique des commandes et l'analyse syntaxique.
4. **Composables UI M3 & Dock** :
   - `HomeScreen` avec grille des applications épurée.
   - `MacosDock` flottant avec animation, élévation et navigation fluide.
   - `ConsoleScreen` type terminal sombre avec coloration syntaxique simple.
   - `SettingsScreen` avec sélection manuelle des paquets et visionneuse/exportateur de documentation.
5. **Documentation & CI GitHub** :
   - Fichier de documentation complet (`ECOSYSTEM_SPEC.md` / `manifest.json`) intégrable et exportable.
   - Fichier de workflow GitHub Actions (`.github/workflows/android.yml`) pour la compilation automatique.
6. **Vérification & Compilation** :
   - Validation via `compile_applet`.
