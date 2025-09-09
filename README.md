# Expense Tracker 💸

A modern, native Android expense tracker built with **100% Kotlin** and **Jetpack Compose**. This app provides a seamless experience for managing personal finances, tracking group expenses, and splitting bills with friends, all powered by a robust Spring Boot backend.

---

## ✨ Core Features

| Feature | Description | Screenshot |
| :--- | :--- | :--- |
| **Google Sign-In** | Secure and easy authentication using your Google account. | <img src="https://github.com/KevalMGajjar/ExpenseTrackerAndroid/blob/KevalMGajjar-ui-photos/login.jpg" width="200" alt="Login with Google"/> |
| **Effortless Expense Logging** | Quickly record what you spend, who you're with, and how it's split with a clean, intuitive UI. | <img src="https://github.com/KevalMGajjar/ExpenseTrackerAndroid/blob/KevalMGajjar-ui-photos/expense.jpg" width="200" alt="Add Expense"/> |
| **Settle Up with Friends** | Easily track who owes whom and settle balances in a tap. | <img src="https://github.com/KevalMGajjar/ExpenseTrackerAndroid/blob/KevalMGajjar-ui-photos/SettleUpUi.jpg" width="200" alt="Settle Up"/> |
| **Group Spending Made Easy** | Manage shared expenses in groups. Add members and track spending all in one place. | <img src="https://github.com/KevalMGajjar/ExpenseTrackerAndroid/blob/KevalMGajjar-ui-photos/GroupUi.jpg" width="200" alt="Group Details"/> |
| **Clean Dashboard** | Get a quick overview of your total balance and recent activities at a glance. | <img src="https://github.com/KevalMGajjar/ExpenseTrackerAndroid/blob/KevalMGajjar-ui-photos/DashBoardUi.jpg" width="200" alt="Dashboard View"/> |

---

## 🏗️ Architecture & Tech Stack

This project follows modern Android development best practices, utilizing an **MVVM architecture** to create a scalable and maintainable codebase.

### Android (Client)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material3](https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=material-design&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-E28B59?style=for-the-badge&logo=kotlin&logoColor=white)
![Hilt](https://img.shields.io/badge/Hilt-007396?style=for-the-badge&logo=docusign&logoColor=white)
![RoomDB](https://img.shields.io/badge/Room_DB-D4554A?style=for-the-badge&logo=sqlite&logoColor=white)
![Retrofit](https://img.shields.io/badge/Retrofit-469490?style=for-the-badge&logo=square&logoColor=white)
![OkHttp](https://img.shields.io/badge/OkHttp-469490?style=for-the-badge&logo=square&logoColor=white)

- **UI:** Jetpack Compose & Material 3
- **Asynchronous:** Kotlin Coroutines
- **Architecture:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt
- **Local Storage:** Room DB

### Backend (Server)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

- **Framework:** Spring Boot with Kotlin
- **Database:** MongoDB
- **Authentication:** JWT (JSON Web Tokens)
- **API:** REST API

---

## 🚀 Getting Started

To get the Android app running on your device:

1.  **Clone the repo:**
    ```bash
    git clone [https://github.com/KevalMGajjar/ExpenseTrackerAndroid.git](https://github.com/KevalMGajjar/ExpenseTrackerAndroid.git)
    ```

2.  **Project Configuration:**
    - Open the project in Android Studio (latest stable version recommended).
    - Navigate to the `utils` folder and open the `Constants.kt` file.
    - Update the following values with your own configuration:
        - `BASE_URL`: The URL of your running backend server.
        - `JWT_SECRET`: Your secret key for JWT.
        - `GOOGLE_SERVICE_STRING`: Your Google services string.

3.  **Backend Setup:**
    - This app requires the Spring Boot backend to be running.
    - You can find the backend repository here: **[KevalMGajjar/ExpenseTrackerBackend](https://github.com/KevalMGajjar/ExpenseTrackerBackend)**
    - Clone and run the backend server separately, following the instructions in its README.

4.  **Build & Run:**
    - Let Gradle sync all the dependencies.
    - Hit the 'Run' button and select your emulator or physical device.

---

<p align="center">
  Made with ❤️ by Keval Gajjar
</p>
