# Setup Guide for Task Management System

## Prerequisites

| Tool | Required Version |
|------|-----------------|
| Java | 25.0.2+ |
| Maven | 3.9.14+ |
| Node.js | 18+ |
| Flutter | 3.x+ |
| PostgreSQL | 14+ |

---

## Backend Setup (Spring Boot)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/nhathao428/task-management-system.git
   cd task-management-system
   ```

2. **Configure Environment Variables**:
   - Create a `.env` file (or set via `application.properties`) in the `backend` directory:
     ```
     DATABASE_URL=jdbc:postgresql://localhost:5432/task_management
     DATABASE_USERNAME=<your_db_user>
     DATABASE_PASSWORD=<your_db_password>
     JWT_SECRET=<your_jwt_secret>
     SERVER_PORT=8080
     ```

3. **Build and Run the Backend**:
   ```bash
   cd backend
   mvn clean install
   mvn spring-boot:run
   ```
   - The API will be available at `http://localhost:8080`.

---

## Frontend Setup (React + Vite)

1. **Navigate to the Frontend Directory**:
   ```bash
   cd frontend
   ```

2. **Install Dependencies**:
   ```bash
   npm install
   ```

3. **Run the Frontend Application**:
   ```bash
   npm run dev
   ```
   - Access the application at `http://localhost:5173`.

4. **Build for Production**:
   ```bash
   npm run build
   ```

---

## Mobile Setup (Flutter)

1. **Install Flutter SDK** — see https://flutter.dev/docs/get-started/install.

2. **Navigate to the Mobile Directory**:
   ```bash
   cd mobile
   ```

3. **Install Dependencies**:
   ```bash
   flutter pub get
   ```

4. **Run the Mobile Application**:
   - For Android:
     ```bash
     flutter run
     ```
   - For a specific device:
     ```bash
     flutter run -d <device_id>
     ```

5. **Set Up Android Environment** (if developing for Android):
   - Install Android Studio and set up an Android Virtual Device (AVD).
   - Configure environment variables in `.bash_profile` or `.zshrc`:
     ```bash
     export ANDROID_HOME=$HOME/Library/Android/sdk
     export PATH=$PATH:$ANDROID_HOME/emulator
     export PATH=$PATH:$ANDROID_HOME/tools
     export PATH=$PATH:$ANDROID_HOME/tools/bin
     export PATH=$PATH:$ANDROID_HOME/platform-tools
     ```

---

### Notes:
- Ensure PostgreSQL is running and the database schema has been applied (see `docs/DATABASE_SCHEMA.md`).
- For any issues, please refer to the [GitHub Issues](https://github.com/nhathao428/task-management-system/issues).

