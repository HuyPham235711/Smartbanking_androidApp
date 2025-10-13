# 🚀 Quick Start — Android Mobile App

### 🧩 Môi trường

| Thành phần             | Phiên bản                       |
| ---------------------- | ------------------------------- |
| Android Studio         | **Koala 2024.2.1** hoặc mới hơn |
| Gradle                 | **8.7**                         |
| Android Gradle Plugin  | **8.5.1**                       |
| compileSdk / targetSdk | **34**                          |
| minSdk                 | **24**                          |

---

### ⚙️ 1️⃣ Setup ban đầu

```bash
git clone https://github.com/<team>/<repo>.git
cd <repo>
```

Mở bằng **Android Studio → File → Sync Project with Gradle Files**

---

### 🔑 2️⃣ Firebase

* Project: **android-final-f73c9**
* `project_id`: **android-final-f73c9-34887**
* File `google-services.json` nằm tại `app/`
  (Nếu thiếu → tải lại trong Firebase Console → Project Settings → Your apps → Android)

---

### 📦 3️⃣ Dependency chính

```gradle
implementation platform('com.google.firebase:firebase-bom:33.1.2')
implementation 'com.google.firebase:firebase-auth'
implementation 'com.google.firebase:firebase-firestore'
implementation "androidx.navigation:navigation-fragment-ktx:2.8.3"
implementation "androidx.navigation:navigation-ui-ktx:2.8.3"
```

---

### 🧱 4️⃣ Build & Run

* **Build → Clean Project → Rebuild Project**
* **Run app**
* Nếu lỗi Firebase: kiểm tra `google-services.json`
* Nếu lỗi Navigation: thêm `xmlns:app="http://schemas.android.com/apk/res-auto"`

---

### 🧾 5️⃣ Quy tắc Git

| File                                        | Commit? | Ghi chú             |
| ------------------------------------------- | ------- | ------------------- |
| `build.gradle`, `gradle-wrapper.properties` | ✅       | Giữ version đồng bộ |
| `google-services.json`                      | ✅       | Không chứa secret   |
| `local.properties`, `/build/`               | ❌       | Máy cục bộ          |

---

