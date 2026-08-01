# NotifyAuto

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/parroz4/notify-auto)](https://github.com/parroz4/notify-auto/releases/latest)

**Forward notifications from any Android app to your car's Android Auto screen.**

*🇮🇹 [Versione italiana più sotto](#-italiano)*

---

## The problem

Android Auto only displays notifications from messaging apps (`MessagingStyle`). Everything else — your bank, your parcel tracker, your smart home, your calendar — is silently dropped, so you end up picking up the phone while driving.

## What NotifyAuto does

It listens to your phone's notifications, and re-publishes the ones you choose in the format Android Auto understands. They then show up on the car screen, and Google Assistant can read them aloud.

Nothing is installed in the car: the phone does all the work.

## Features

- ✅ **Pick the apps you want** — a switch per installed app
- 🔍 **Search bar** by app name or package name
- ☑️ **Enable all / Disable all**, applied to the apps matching your current search
- ⭐ **Enabled apps pinned to the top**, with a counter, so you can turn them off fast
- 📂 **Grouped by category** (Games, Social, Music, …)
- 🚗 **"Forward only while connected to Android Auto"** (on by default) — connection is detected automatically
- 📋 **A screen inside Android Auto**: the list of forwarded notifications, tap a row to dismiss it, or *Clear all*
- 🧹 **Self-cleaning**: dismissing or reading the original notification on the phone removes the forwarded copy from the car too
- 🔇 Skips ongoing notifications (music players, foreground services) and group summaries
- 🌍 English and Italian

## Install

📥 **[Download the latest APK](https://github.com/parroz4/notify-auto/releases/latest)**

### Automatic updates with Obtainium (recommended)

[Obtainium](https://github.com/ImranR98/Obtainium) installs and updates apps straight from GitHub releases, like a store:

1. Install Obtainium (from [its releases](https://github.com/ImranR98/Obtainium/releases/latest))
2. Open Obtainium → **Add App**
3. Paste this repository URL: `https://github.com/parroz4/notify-auto`
4. Tap **Add** — you will be notified on every new release

### Setup

1. Install the APK
2. If Android blocks the permission: **Settings → Apps → NotifyAuto → ⋮ → Allow restricted settings** (needed for apps installed outside the Play Store)
3. Open NotifyAuto → **Grant notification access**
4. Enable the apps you want to forward
5. In **Android Auto → Settings**, tap "Version" 10 times to unlock developer settings, then enable **Unknown sources**
6. Connect your phone to the car

> **Why isn't this on the Play Store?** Google only allows a fixed set of app categories on Android Auto, and forwarding non-messaging notifications isn't one of them. Sideloading is the only option — the same is true for every app of this kind.

## How it works

1. `NotificationForwarderService` (a `NotificationListenerService`) receives every notification posted on the phone
2. If the source app is one you enabled, the notification is re-published as a `MessagingStyle` notification carrying `CarExtender` and `CarAppExtender`, with *Reply* / *Mark as read* actions (Android Auto requires them to display it)
3. `CarScreenService` (a `CarAppService`) puts NotifyAuto in the Android Auto launcher, showing the list of forwarded notifications

*Reply* and *Mark as read* simply dismiss the forwarded copy — they do not interact with the source app.

## Build

Requires JDK 17 and the Android SDK (API 34):

```bash
gradle assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Requirements

- Android 8.0+ (minSdk 26)
- The Android Auto app installed on the phone

## License

[MIT](LICENSE)

---

## 🇮🇹 Italiano

**Inoltra le notifiche di qualsiasi app Android sullo schermo di Android Auto.**

Android Auto mostra solo le notifiche in stile "messaggistica": quelle delle app normali (banca, corriere, casa smart, calendario) vengono ignorate, e finisci per prendere in mano il telefono mentre guidi. NotifyAuto le intercetta e le ripubblica in un formato che Android Auto visualizza e che l'Assistente può leggere ad alta voce. In auto non si installa nulla: fa tutto il telefono.

### Funzionalità

- ✅ Scegli quali app inoltrare, con un interruttore per ogni app installata
- 🔍 Barra di ricerca per nome app o package
- ☑️ "Attiva tutte" / "Disattiva tutte", applicati alle sole app filtrate dalla ricerca
- ⭐ App attive in cima alla lista, con il conteggio, per disattivarle al volo
- 📂 App raggruppate per categoria (Giochi, Social, Musica, …)
- 🚗 "Inoltra solo quando connesso ad Android Auto" (attivo di default), con rilevamento automatico
- 📋 Una schermata dentro Android Auto: l'elenco delle notifiche inoltrate, tocca una riga per cancellarla oppure "Cancella tutte"
- 🧹 Pulizia automatica: quando leggi o cancelli la notifica originale sul telefono, la copia sparisce anche dall'auto
- 🔇 Salta notifiche persistenti (musica, servizi in background) e riepiloghi di gruppo

### Installazione

📥 **[Scarica l'ultimo APK](https://github.com/parroz4/notify-auto/releases/latest)**, oppure aggiungi `https://github.com/parroz4/notify-auto` a [Obtainium](https://github.com/ImranR98/Obtainium) per gli aggiornamenti automatici.

1. Installa l'APK
2. Se Android blocca il permesso: **Impostazioni → App → NotifyAuto → ⋮ → Consenti impostazioni con limitazioni**
3. Apri NotifyAuto → **Concedi accesso alle notifiche**
4. Attiva le app da inoltrare
5. In **Android Auto → Impostazioni**, tocca 10 volte "Versione" per sbloccare le opzioni sviluppatore, poi attiva **Sorgenti sconosciute**
6. Collega il telefono all'auto

> **Perché non è sul Play Store?** Google ammette su Android Auto solo categorie precise di app, e l'inoltro di notifiche non-messaggistica non rientra in nessuna. Il sideload è l'unica strada, come per tutte le app di questo tipo.

### Requisiti

- Android 8.0 o superiore (minSdk 26)
- App Android Auto installata sul telefono
