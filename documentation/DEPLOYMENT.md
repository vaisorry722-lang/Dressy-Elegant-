# DRESSY ELEGANT — Development & Deployment Guide

## Build & Verify
- Compile Kotlin & APK: `gradle :app:compileDebugKotlin` / `gradle :app:assembleDebug`
- Run Unit Tests: `gradle :app:testDebugUnitTest`
- Run Firestore Security Rules Tests (with Firestore emulator on port `8085`):
  ```bash
  FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
  ```

## Security Checklist
- Never store plaintext Security PINs (only salted SHA-256 digests are persisted locally per UID).
- Never promote `subscriptionTier` to `PREMIUM` from client code; payment references sent to `301620556052` remain `PENDING_VERIFICATION` until verified server-side.
- Always deploy `firestore.rules` after any schema update.
