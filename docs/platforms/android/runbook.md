# Android POS Runbook [PARITY LANE]

Aplikasi **Cassy Android POS** berfungsi sebagai lane pendukung untuk mobilitas di dalam toko dan sebagai validator *business semantics* lintas platform.

## 1. Lingkungan Pengembangan
- **JDK**: Amazon Corretto 17.
- **Android SDK**: API Level 34 (Target), API Level 24 (Minimum).
- **IDE**: Android Studio.

## 2. Cara Menjalankan
Gunakan Gradle task atau jalankan langsung dari Android Studio:
```bash
./gradlew :apps:android-pos:installDebug
```

## 3. Karakteristik Platform
- **UI**: Menggunakan Jetpack Compose.
- **Hardware Integration**:
  - **Printer**: Bluetooth ESC/POS atau Wi-Fi Thermal Printer.
  - **Scanner**: Camera-based scanning (ML Kit) atau Bluetooth HID scanner.
- **SQLite**: Menggunakan `AndroidSqliteDriver` bawaan sistem.

## 4. Status Paritas (Parity Status)
Android Lane harus mengikuti logika bisnis yang sama dengan Desktop Lane melalui modul `:shared`. Namun, beberapa fitur mungkin dibatasi:
- **Reporting**: Terbatas pada ringkasan shift harian.
- **Inventory**: Fokus pada Stock Take (Opname) dan Receiving di gudang.

## 5. Deployment
Aplikasi didistribusikan melalui file APK/AAB atau internal app sharing.
```bash
./gradlew :apps:android-pos:assembleRelease
```
