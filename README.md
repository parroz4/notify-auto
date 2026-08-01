# NotifyAuto

Inoltra le notifiche di qualsiasi app Android sullo schermo di **Android Auto**.

Android Auto mostra solo le notifiche in stile "messaggistica" (MessagingStyle): quelle di app normali (banca, meteo, promemoria…) vengono ignorate. NotifyAuto le intercetta sul telefono e le ripubblica come conversazioni compatibili con Android Auto, che così le visualizza sullo schermo dell'auto e può leggerle ad alta voce tramite l'Assistente Google.

## Funzionalità

- ✅ Scegli quali app inoltrare (interruttore per ogni app installata)
- 🔍 Barra di ricerca per nome app o package
- ☑️ Pulsanti "Attiva tutte" / "Disattiva tutte", applicati alle sole app filtrate dalla ricerca
- 📂 App raggruppate per categoria (Giochi, Social, Musica, …)
- 🚗 Opzione "Inoltra solo quando connesso ad Android Auto" (attiva di default) — rileva la connessione interrogando il content provider di Android Auto, senza dipendenze extra
- 🔇 Salta notifiche persistenti (musica, servizi in foreground) e riepiloghi di gruppo
- 🔁 Nessun loop: le notifiche generate dall'app stessa non vengono mai rielaborate

## Come funziona

1. `NotificationForwarderService` (un `NotificationListenerService`) intercetta ogni notifica del telefono
2. Se l'app di origine è tra quelle selezionate, la notifica viene ripubblicata come `MessagingStyle` con azioni *Rispondi* / *Segna come letto* (richieste da Android Auto per visualizzarla)
3. Android Auto la mostra tra le notifiche e l'Assistente può leggerla

Le azioni *Rispondi* / *Segna come letto* chiudono semplicemente la notifica inoltrata: non interagiscono con l'app di origine.

## Download

📥 **[Scarica l'ultima versione](https://github.com/parroz4/notify-auto/releases/latest)** dalla sezione Releases.

### Aggiornamenti automatici con Obtainium (consigliato)

[Obtainium](https://github.com/ImranR98/Obtainium) installa e aggiorna le app direttamente dalle release GitHub, come uno store:

1. Installa Obtainium (dalle [sue release](https://github.com/ImranR98/Obtainium/releases/latest))
2. Apri Obtainium → **Aggiungi app**
3. Incolla l'URL di questo repository: `https://github.com/parroz4/notify-auto`
4. Tocca **Aggiungi**: Obtainium installa l'ultima versione e ti avviserà a ogni nuova release

## Installazione

1. Scarica l'APK dalle [Releases](https://github.com/parroz4/notify-auto/releases/latest) (o compilalo con `gradle assembleDebug`) e installalo sul telefono
2. Se Android blocca il permesso: **Impostazioni → App → NotifyAuto → ⋮ → Consenti impostazioni con limitazioni** (necessario per le app installate fuori dal Play Store)
3. Apri NotifyAuto → **Concedi accesso alle notifiche**
4. Attiva le app da inoltrare (con ricerca, selezione multipla e raggruppamento per categoria)
5. In **Android Auto → Impostazioni**, tocca 10 volte "Versione" per sbloccare le opzioni sviluppatore, poi attiva **Sorgenti sconosciute**
6. Collega il telefono all'auto

## Build

Richiede JDK 17 e Android SDK (API 34):

```bash
gradle assembleDebug
```

L'APK viene generato in `app/build/outputs/apk/debug/app-debug.apk`.

## Requisiti

- Android 8.0+ (minSdk 26)
- App Android Auto installata sul telefono
