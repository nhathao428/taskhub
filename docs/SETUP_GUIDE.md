# Setup Guide for Task Management System

## Backend Setup
1. **Clone the repository**:
   ```bash
   git clone https://github.com/nhathao428/task-management-system.git
   cd task-management-system
   ```
2. **Install Dependencies**:
   - Make sure you have Node.js installed.
   - Navigate to the backend directory and run:
   ```bash
   npm install
   ```
3. **Configure Environment Variables**:
   - Create a `.env` file in the backend directory and set the following variables:
     ```
     DATABASE_URL=<your_database_url>
     JWT_SECRET=<your_jwt_secret>
     PORT=5000
     ```
4. **Run the Backend**:
   ```bash
   npm start
   ```

## Frontend Setup
1. **Navigate to the Frontend Directory**:
   ```bash
   cd frontend
   ```
2. **Install Dependencies**:
   - Ensure you have Node.js installed, then run:
   ```bash
   npm install
   ```
3. **Run the Frontend Application**:
   ```bash
   npm start
   ```
   - Access the application at `http://localhost:3000`.

## Mobile Development Environment Setup
1. **Install Dependencies**:
   - Make sure you have React Native CLI installed globally:
   ```bash
   npm install -g react-native-cli
   ```
2. **Set Up Android Environment** (if developing for Android):
   - Install Android Studio and set up an Android Virtual Device (AVD).
   - You may need to configure your environment variables in `.bash_profile` or `.zshrc`:
     ```bash
     export ANDROID_HOME=$HOME/Library/Android/sdk
     export PATH=$PATH:$ANDROID_HOME/emulator
     export PATH=$PATH:$ANDROID_HOME/tools
     export PATH=$PATH:$ANDROID_HOME/tools/bin
     export PATH=$PATH:$ANDROID_HOME/platform-tools
     ```
3. **Run the Mobile Application**:
   - Make sure to navigate to the mobile directory:
   ```bash
   cd mobile
   ```
   - Run the application for Android:
   ```bash
   react-native run-android
   ```
   - Or for iOS:
   ```bash
   react-native run-ios
   ```

---

### Notes:
- Ensure to check the repository for any additional configuration or dependencies specific to your operating system.
- For any issues, please refer to the [GitHub Issues](https://github.com/nhathao428/task-management-system/issues).
