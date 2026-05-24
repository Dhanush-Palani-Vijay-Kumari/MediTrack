# MediTrack 💊

A production-grade Android healthcare companion app built with **Kotlin**, **Jetpack Compose**, and **Clean Architecture (MVVM + Repository)**. Helps patients track medications, log vitals, and manage doctor appointments.

---

## Screenshots

| Home Dashboard | Vitals Tracker | Medication Detail |
|---|---|---|
| *Add screenshots here* | *Add screenshots here* | *Add screenshots here* |

---

## Features

- **Medication tracking** — Schedule doses (1–3x daily), mark as taken, track pill counts
- **Streak counter** — Consecutive days of full adherence
- **Vitals logging** — Blood pressure, heart rate, blood sugar, weight, SpO₂ with trend charts
- **Smart status badges** — Normal / Elevated / Watch / High based on clinical ranges
- **Appointment manager** — Add doctor appointments, get notified 1 day before
- **WorkManager reminders** — Fires even when app is closed or device restarts
- **Offline-first** — Everything works without internet via Room database
- **Dark mode** — Full Material Design 3 dark theme support

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture + Repository Pattern |
| Async | Kotlin Coroutines + Flow |
| DI | Hilt |
| Local DB | Room + SQLite |
| Background | WorkManager |
| Notifications | Firebase Cloud Messaging |
| Crash reporting | Firebase Crashlytics |
| Testing | JUnit + Mockito + Espresso |
| CI/CD | GitHub Actions |

---

## Project Structure

```
app/src/main/java/com/meditrack/
├── data/
│   ├── local/
│   │   ├── dao/         # Room DAOs (MedicationDao, VitalsDao, AppointmentDao)
│   │   ├── entity/      # Room entities
│   │   └── MediTrackDatabase.kt
│   └── repository/      # Repositories (single source of truth)
├── di/                  # Hilt DI modules
├── ui/
│   ├── home/            # Home screen + ViewModel
│   ├── medications/     # Medications list, detail, add screen
│   ├── vitals/          # Vitals tracker + sparkline charts
│   ├── appointments/    # Appointments list + add screen
│   ├── profile/         # User profile + settings
│   └── theme/           # Colors, Typography, Theme
├── worker/              # WorkManager reminder workers
├── util/                # ReminderScheduler
├── MainActivity.kt
├── MediTrackApplication.kt
└── MediTrackMessagingService.kt
```

---

## Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 35
- A Firebase project (free tier works fine)

### Steps

1. **Clone the repo**
   ```bash
   git clone https://github.com/YOUR_USERNAME/meditrack.git
   cd meditrack
   ```

2. **Set up Firebase**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Create a new project named `MediTrack`
   - Add an Android app with package name `com.meditrack`
   - Download `google-services.json` and place it in `app/`
   - Enable **Firestore**, **Authentication**, **Cloud Messaging**, and **Crashlytics**

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```
   Or open in Android Studio and click Run ▶

### Running tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (needs connected device or emulator)
./gradlew connectedAndroidTest
```

---

## Architecture

```
UI Layer (Compose)
    ↕ StateFlow / collectAsStateWithLifecycle
ViewModel Layer (HiltViewModel)
    ↕ suspend functions / Flow
Repository Layer
    ↕              ↕
Room (local)   Firebase (remote)
```

Data flows **one direction** — UI observes StateFlow from ViewModel, ViewModel collects from Repository, Repository combines local Room data with optional remote sync.

---

## CI/CD

GitHub Actions runs on every push to `main` or `develop`:
1. Runs all unit tests
2. Builds a debug APK
3. Uploads APK as a build artifact

---

## Roadmap

- [ ] Firebase Auth (email + Google Sign-In)
- [ ] Firestore sync for multi-device support
- [ ] Medication refill reminders
- [ ] Export health data as PDF
- [ ] Wearable companion (Wear OS)
- [ ] Biometric lock

---

## License

MIT License — see [LICENSE](LICENSE) for details.
