# Student Attendance System

An Android application (Java/Kotlin) for managing **student attendance** with a built-in **Kiosk Mode** that locks the device to the attendance screen.

---

## Features

| Feature | Description |
|---|---|
| **Mark Attendance** | Tap Present / Late / Absent for each student |
| **Bulk Mark Present** | One-tap "Mark All Present" button |
| **Save Attendance** | Persists all records to a local Room (SQLite) database |
| **Kiosk Mode** | Pins the app to the screen; prevents students from navigating away |
| **Admin PIN** | Protected admin access (default PIN: `1234`) |
| **Student Management** | Add, edit, and remove students from the Admin panel |
| **Attendance Reports** | View attendance percentage and P/A/L counts per student |
| **Boot Persistence** | Kiosk mode survives device reboots |

---

## Architecture

```
app/
├── data/          # Room database, entities (Student, AttendanceRecord), DAOs, Repository
├── viewmodel/     # StudentViewModel, AttendanceViewModel (AndroidViewModel + LiveData)
├── adapter/       # RecyclerView adapters (marking, summary, management)
├── kiosk/         # KioskManager — lock-task, immersive mode, PIN management
├── receiver/      # KioskDeviceAdminReceiver, BootReceiver
└── ui/            # Activities: Main, Admin, AddStudent, AttendanceReport
```

**Tech stack:** Kotlin · Room · LiveData · ViewModel · RecyclerView · Material Components

---

## Getting Started

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 24+ (minSdk 24, targetSdk 34)
- Gradle 8.4

### Build & Run
```bash
# Clone the repository
git clone https://github.com/sachin712122/app.git
cd app

# Open in Android Studio and click "Run", or build from command line:
./gradlew assembleDebug
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

---

## Kiosk Mode

### Quick activation (no device owner required)
1. Open the app.
2. Tap the **lock icon** in the toolbar (or overflow menu → "Enable Kiosk Mode").
3. Enter the admin PIN (`1234` by default).
4. The app will enter immersive full-screen mode. The back button is disabled.

### Exit kiosk mode
1. Tap the lock icon again and enter the admin PIN.

### Full kiosk (device owner — recommended for production)
To prevent all system navigation (home button, recent apps):
```bash
# Set the app as Device Owner via ADB (device must have no accounts)
adb shell dpm set-device-owner com.attendance.app/.receiver.KioskDeviceAdminReceiver
```
After setting as device owner, the app uses Android's `startLockTask()` API for proper pinned-screen kiosk mode.

### Default Admin PIN
`1234` — change it immediately from **Admin Panel → Change Admin PIN**.

---

## Admin Panel

Access via the overflow menu → **Admin** (requires PIN).

From the Admin panel you can:
- **Add / Edit / Remove** students
- **Toggle Kiosk Mode**
- **Change the Admin PIN**
- **View Attendance Reports** (percentage breakdown per student)

---

## Database Schema

| Table | Columns |
|---|---|
| `students` | `id`, `name`, `rollNumber`, `className`, `isActive`, `createdAt` |
| `attendance_records` | `id`, `studentId` (FK), `date` (YYYY-MM-DD), `status` (PRESENT/ABSENT/LATE), `markedAt` |

---

## Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

---

## Permissions

| Permission | Reason |
|---|---|
| `RECEIVE_BOOT_COMPLETED` | Re-activate kiosk mode after reboot |
| `DISABLE_KEYGUARD` | Prevent lock screen in kiosk mode |
| `WAKE_LOCK` | Keep screen on during attendance session |
| `FOREGROUND_SERVICE` | (reserved for future background sync) |
