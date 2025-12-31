# Lantern Development Roadmap

Lantern is a mobile Android application that turns your device into a local file server, allowing easy file access via a web browser on the local network.

## Phase 1: Foundation & Android Setup
- [x] **Migrate to Android Project Structure**
    - [x] Update `settings.gradle.kts` and `app/build.gradle.kts` for Android support.
    - [x] Create `AndroidManifest.xml`.
    - [x] Structure source directories for Android (`src/main/java`, `src/main/res`).
- [x] **Asset Integration**
    - [x] Import `DepartureMono.otf` into Android assets/font resources.
    - [x] Source/Generate `ModernSans` font (Using System Sans).
- [x] **Dependency Management**
    - [x] Add AndroidX, Jetpack Compose, and Material 3 dependencies.

## Phase 2: UI & Theming (Modern Green)
- [x] **Theme Setup**
    - [x] Configure `Theme.kt` with a Modern Green color palette.
    - [x] Set up Typography to use `ModernSans` (Headings) and `DepartureMono` (Content).
- [x] **Main Screen Layout**
    - [x] Status Indicator (Server On/Off).
    - [x] IP Address display (big, legible).
    - [x] Port configuration (optional but good).
    - [x] Start/Stop button.

## Phase 3: Core Server Functionality
- [x] **HTTP Server Integration**
    - [x] Integrate **Ktor Server** (Netty/CIO) or **NanoHTTPD** for Android.
    - [x] Create a service to run the server in the background.
- [x] **Web Interface (The Hosted Site)**
    - [x] Design a simple, responsive HTML/CSS page to serve as the directory listing.
    - [x] Implement file download capability.

## Phase 4: File System & Permissions
- [x] **Permissions Handling**
    - [x] Request file access permissions (READ_EXTERNAL_STORAGE / MANAGE_EXTERNAL_STORAGE depending on target SDK).
- [x] **File Indexing**
    - [x] Logic to list files and folders on the device.
    - [x] Map internal file paths to URL paths.

## Phase 5: Polishing & Build
- [ ] **Testing**
    - [ ] Local network connectivity tests.
    - [ ] Large file transfer stability.
- [ ] **Packaging**
    - [ ] Configure `signingConfig` (debug).
    - [ ] Generate APK.

## Phase 6: Future (Web & Security)
- [ ] **Internet Exposure** (Tunneling/Port Forwarding).
- [ ] **Security** (Basic Auth, HTTPS).