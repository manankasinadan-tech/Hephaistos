# Hephaistos Launcher

> **Lanceur épuré et hôte d'orchestration pour l'écosystème modulaire de portage GSI et partitions système sous Android.**  
> Package ID : `com.hephaistos.launcher`

[![Build Hephaistos Android App](https://github.com/hephaistos/launcher/actions/workflows/android.yml/badge.svg)](https://github.com/hephaistos/launcher/actions)
[![Ecosystem Spec](https://img.shields.io/badge/Ecosystem-Specification-orange.svg)](./ECOSYSTEM_SPEC.md)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

---

## 📖 Guide de Création des Modules de l'Écosystème
> [!IMPORTANT]
> **Vous êtes un développeur ou une IA souhaitant concevoir une nouvelle application constitutive de l'environnement ?**  
> Consultez sans attendre la documentation officielle complète :  
> 👉 **[SPÉCIFICATION OFFICIELLE DE L'ÉCOSYSTÈME (ECOSYSTEM_SPEC.md)](./ECOSYSTEM_SPEC.md)** 👈  
> Elle détaille le protocole IPC standardisé, le schéma `manifest.json`, le formalisme JSON des commandes et un modèle Kotlin prêt à l'emploi.

---

## 🛠️ Philosophie d'Hephaistos

Dans le monde du portage de Generic System Images (GSI) et de modification de partitions Android (`system`, `vendor`, `product`, `payload.bin`, `erofs`), les outils sont souvent disparates ou regroupés dans de gros scripts complexes.

**Hephaistos** introduit un nouveau paradigme :
1. **Écosystème Découplé** : Chaque future application est un outil/moteur indépendant avec sa propre spécialité.
2. **Contrôle Universel** : Chaque module peut être piloté par un autre module, par le lanceur Hephaistos, ou par un orchestrateur d'IA sans connaître son code source grâce à son `manifest.json`.
3. **Lanceur Épuré** : Hephaistos affiche **exclusivement** les applications de notre environnement (package commençant par `com.hephaistos.*` ou sélectionnées manuellement), sans surcharge visuelle ni pollution d'applications tierces.

---

## ✨ Fonctionnalités Clés

### 1. Accueil Minimaliste & Épuré
- Horloge et date élégantes avec indicateur de statut de l'écosystème.
- Grille fluide Material Design 3 affichant uniquement les modules du projet.
- Retour haptique et animation au lancement des modules.

### 2. Dock Flottant Style macOS (M3)
- Barre inférieure surélevée semi-transparente avec coins arrondis :
  - **Rafraîchir** : Re-scanne instantanément les paquets installés avec animation circulaire.
  - **Console (Terminal)** : Ouvre le terminal de commande IPC interactif.
  - **Paramètres** : Ouvre le panneau de configuration.

### 3. Console Terminal IPC
- Permet d'envoyer n'importe quelle commande aux modules connectés avec arguments JSON :
  ```bash
  ping com.hephaistos.unpacker
  send com.hephaistos.unpacker extract_partition {"source":"system.img","partition":"system"}
  ```
- Historique d'exécution, affichage couleur Monospace et formatage JSON automatique.
- Mode simulation (`demo`) pour tester la boucle IPC en temps réel.

### 4. Paramètres Avancés
- **Gestionnaire de Thème** : Mode Système, Clair ou Sombre (Obsidian Forge).
- **Sélection Manuelle d'Applications** : Permet de choisir manuellement parmi toutes les applications installées sur le périphérique celles à inclure dans l'environnement Hephaistos.
- **Visionneuse & Export de Documentation** : Visualisez et exportez en 1 clic le schéma `manifest.json` et la spécification `README.md` via le système de partage Android.

---

## 🏗️ Compilation & Intégration Continue (GitHub Actions)

Ce projet est pré-configuré pour être pushé sur GitHub et compilé automatiquement à chaque commit via GitHub Actions.

### Fichier Workflow : `.github/workflows/android.yml`
```yaml
name: Build Hephaistos Android App
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
    - run: ./gradlew assembleDebug
```

L'APK compilé (`app-debug.apk`) est généré et mis à disposition dans les **Artifacts** de chaque exécution du workflow.

---

## 📄 Licence
Sous licence Apache 2.0. Développé pour la communauté de portage GSI et partitions Android.
