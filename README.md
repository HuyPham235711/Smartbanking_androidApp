# 💳 Smartbanking Android App

> Ứng dụng ngân hàng điện tử được phát triển bằng **Kotlin + Jetpack Compose + Firebase**


## 🧱 (14/10/2025)**Cập nhật lớn: Refactor dự án sang mô hình MVVM (Model–View–ViewModel)**

### 📌 Mục tiêu

Chuẩn hoá kiến trúc toàn bộ project theo **MVVM pattern** và **Jetpack Compose UI**, giúp mã nguồn:

* Dễ bảo trì & mở rộng.
* Tách biệt rõ giữa UI, logic nghiệp vụ, và dữ liệu.
* Không còn phụ thuộc vào XML layout cũ.

---

### 🔧 **Thay đổi chính trong cấu trúc thư mục**

```
com.example.afinal
├── data                ← Model (dữ liệu, DAO, Repository)
│   └── account
│       ├── Account.kt
│       ├── AccountDao.kt
│       ├── AccountRepository.kt
│       └── AppDatabase.kt
│
├── viewmodel           ← ViewModel (xử lý logic, state)
│   └── account
│       └── AccountViewModel.kt
│
└── ui                  ← View (Compose UI)
    └── officer
        └── CreateAccountScreen.kt
```

---

### ⚙️ **Điểm cải tiến chính sau refactor**

✅ Chuyển toàn bộ UI sang **Jetpack Compose** – không còn sử dụng file `.xml`.
✅ Xoá bỏ `findViewById` / `R.layout.*`, thay bằng `@Composable` functions.
✅ Tách biệt hoàn toàn **UI – ViewModel – Repository**.
✅ Dữ liệu hiển thị realtime qua `Flow` + `collectAsState()`.
✅ Dễ dàng mở rộng sang các module khác (Login, Map, Transaction,...).

---

### 🧠 **Kiến trúc mới (MVVM + Compose)**

```
🎨 UI (Compose)
   ⇅ observe via Flow / State
🧠 ViewModel
   ⇅
🧱 Repository
   ⇅
💾 DAO (Room)
   ⇅
🗃️ Database (Room)
```

> 📌 *Jetpack Compose đã thay thế hoàn toàn XML layout trong project.*
> Mọi màn hình mới đều được xây dựng trực tiếp bằng Kotlin và `@Composable` functions.
> Các file `res/layout/*.xml` cũ chỉ còn giữ lại tạm thời cho các phần đặc biệt (như Google Map).

---

### 🚀 **Hướng dẫn team khi phát triển tính năng mới**

Khi thêm module mới (ví dụ `Transaction`, `Login`, `Map`):

1. Tạo **Model + DAO + Repository** trong `data/<feature>`.
2. Tạo **ViewModel** để xử lý logic và expose `Flow/State`.
3. Tạo **Compose UI** trong `ui/<feature>` — không tạo XML.
4. Giao tiếp qua ViewModel (không thao tác trực tiếp với database từ UI).

---

### 👥 **Người thực hiện refactor**

**Phạm Huy** — refactor toàn bộ kiến trúc project sang MVVM + Jetpack Compose,
chuẩn hoá cấu trúc thư mục, dữ liệu, ViewModel, và loại bỏ XML layout cũ.


---

## 🧱 Thành phần & Phiên bản

| Thành phần | Phiên bản / Ghi chú |
|-------------|---------------------|
| **Android Studio** | 🦋 *Koala 2024.1.4* hoặc *Narwhal 2025.1.4* |
| **Gradle** | 8.7+ |
| **Android Gradle Plugin (AGP)** | 8.13.0 |
| **Kotlin** | 2.0.21 |
| **Compose Compiler** | 1.5.15 |
| **Compile SDK** | 36 |
| **Target SDK** | 36 |
| **Min SDK** | 26 |
| **JDK** | 17 |
| **Emulator** | Pixel 6a – Google Play x86_64 (API 34 hoặc 36) |

---

## ⚙️ 1. Cấu hình môi trường

### 🔹 Cài Android Studio
- Tải tại [developer.android.com/studio](https://developer.android.com/studio)
- Khuyến nghị bản **Koala 2024.1.4** hoặc mới hơn.
- Vào `File > Settings > Build, Execution, Deployment > Build Tools > Gradle`:
  - **Use Gradle from:** `gradle-wrapper.properties`
  - **Gradle JDK:** `jbr-17` hoặc `JAVA_HOME` → JDK 17

### 🔹 SDK Platforms
Bật các API sau:
- ✅ Android 14 (API 34)
- ✅ Android 15 (API 35)
- ✅ Android 16 “Baklava” (API 36)
- Tick thêm **Google Play services** và **Android SDK Platform-Tools**

### 🔹 Emulator
- Dùng **Pixel 6a – Google Play x86_64**
- API **34** (ổn định) hoặc **36** (mới nhất)
- RAM ≥ 2GB, enable **Hardware Graphics Acceleration**

---

## 🔧 2. Cấu hình Gradle

**`gradle.properties`**
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
```

**`build.gradle.kts (Project)`**
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

**`build.gradle.kts (Module :app)`**
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.afinal"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.afinal"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
}
```

---

## 🧩 3. Cấu hình thư viện

**`libs.versions.toml`**
```toml
[versions]
agp = "8.13.0"
kotlin = "2.0.21"
coreKtx = "1.17.0"
lifecycleRuntimeKtx = "2.9.4"
activityCompose = "1.11.0"
composeBom = "2024.09.00"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

---

## 🔥 4. Firebase

### Dịch vụ sử dụng:
- Firebase Authentication ✅  
- Firebase Firestore   ✅
- Firebase Analytics ✅  

### Cấu hình file:
Đặt `google-services.json` vào:
```
app/google-services.json
```

Nếu clone repo từ đầu, team member cần xin file này từ **Firebase Console** hoặc từ **team lead**.

---

## ▶️ 5. Chạy dự án

```bash
git clone https://github.com/team/Smartbanking_androidApp.git
cd Smartbanking_androidApp
```

Mở bằng Android Studio → **Sync Gradle**  
Chọn **Pixel 6a (API 34/36)** → Nhấn **Run ▶️**

---

## 🧠 6. Troubleshooting

| Lỗi | Nguyên nhân | Cách khắc phục |
|------|--------------|----------------|
| `Unknown calling package com.google.android.gms` | Emulator preview (API 36) lỗi Play Services | Dùng emulator API 34 Google Play |
| `Firestore NOT_FOUND` | Chưa tạo database | Tạo Firestore DB trong Firebase Console |
| `Unresolved reference compose` | Chưa bật Jetpack Compose | Kiểm tra `buildFeatures { compose = true }` |
| `Android resource linking failed` | minSdk quá thấp | Đặt `minSdk = 26` |

---

## 👨‍💻 Contributors

| Thành viên | Vai trò |
|-------------|----------|
| **Huy Phạm** | Android Dev / Firebase Integration |
| **Team Smartbanking** | UI/UX + Business Logic |

---

## 📦 Ghi chú

> ⚠️ Firestore hiện chưa được bật trong project Firebase.  
> Khi cần test tính năng đọc/ghi dữ liệu, truy cập:  
> [https://console.cloud.google.com/datastore/setup?project=android-final-f73c9-34887](https://console.cloud.google.com/datastore/setup?project=android-final-f73c9-34887)  
> → chọn **Start in test mode** → **Create Database**

## Quy tắc workflow cho team:

 - Chỉ merge vào main qua Pull Request(khác với pull về máy) (không push trực tiếp).

 - Mỗi thành viên tạo nhánh theo quy tắc: <tên>/<scope>/<mô_tả>

  ví dụ:
  
  vy/ui/fix-bottom-navigation
  
  thanh/firebase/auth-register-screen
  
  huywork/env/setup-android-studio

---

## 🏁 Phiên bản hiện tại
**Release Date:** 2025-10-13  
**Build Type:** Debug (Jetpack Compose + Firebase + Navigation)  
**Status:** ✅ Stable, chạy được trên emulator API 34/36
