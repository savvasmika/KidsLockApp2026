Key Capabilities & Architecture
Two Distinct Device Roles:
Parent Device: Professional slate-and-indigo dashboard with real-time child device telemetry (battery level, lock status, connection transport, active theme, and last activity time). Enables instantaneous remote locking and unlocking, generation of time-sensitive 6-digit unlock codes, device renaming, inactivity timeout configuration, and unpairing.
Child Device: Playful, kid-friendly interface with animated themes, mascot avatars, and a break reminder screen ("⭐ TIME TO TAKE A BREAK! ⭐"). Features an [Ask Parent for Code] action that posts live unlock requests to the parent, an interactive 6-digit numeric keypad for code redemption, celebration unlocked screens with active session timers, and an emergency Parent PIN bypass.
Local Wi-Fi Discovery & Pairing:
Uses Android's native Network Service Discovery (NSD / mDNS) registering _kidlock._tcp services over local Wi-Fi.
Features an animated radar scanner on the Parent device and beacon broadcasting on the Child device.
Generates cryptographically secure 6-digit confirmation codes (CryptoUtils.generateSecure6DigitCode()) requiring mutual verification on both screens before establishing a trusted pairing.
Dual-Transport Architecture:
Mode 1 (Local Wi-Fi Peer-to-Peer): Embedded lightweight HTTP/TCP server on the child device handling immediate local commands without external server latency.
Mode 2 (Remote Internet Relay): Clean, structured RemoteBackendService abstraction providing Firebase / Cloud messaging fallback with clear status badges (🟢 Wi-Fi, 🔵 Remote, 🔴 Offline).
12 Thematic Environments for Children:
Includes all 12 custom themes with custom color palettes, background gradients, and mascot personalities:
🌌 Space (Astro)
🎮 Gaming (Pixel)
🚀 Galaxy (Cosmo)
🦖 Dinosaurs (Rexy)
🐼 Animals (Panda & Leo)
🏎 Racing (Turbo)
⚽ Sports (Champ)
🧙 Fantasy (Merlin & Spark)
🌊 Ocean (Splash)
🤖 Robots (Gizmo)
🌈 Colorful (Rainbow)
🌳 Adventure (Scout)
Security & Data Persistence:
Salted SHA-256 PIN hashing and HMAC-SHA256 signature verification for remote commands (CryptoUtils).
Room Database (KidLockDatabase) with strongly typed DAOs and entities for devices, pending unlock requests, active temporary codes (with 2-minute auto-expiration), and app configurations.
Device Administration & Kiosk Support: Registered KidLockDeviceAdminReceiver with device admin policies and Android screen pinning (startLockTask()) integration.
Complete Greek (Ελληνικά) & English Localization:
Full resource localization in values/strings.xml and values-el/strings.xml with dynamic in-app language switching without requiring device restarts.
All unit and Robolectric tests for KIDLOCK have executed and passed (BUILD SUCCESSFUL):
PIN Security & Cryptography: Verified SHA-256 salted PIN hashing, verification, and rejection of invalid PIN attempts.
Pairing & Temporary Code Engine: Verified cryptographically secure 6-digit code generation.
Theme Engine: Verified automatic theme resolution and safe fallback for all 12 custom child environments.
Localization & App Resources: Verified English and Greek string resolution alongside resource binding.
