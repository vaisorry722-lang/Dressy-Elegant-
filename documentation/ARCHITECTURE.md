# DRESSY ELEGANT — Phase 1 Architecture

## Overview
**DRESSY ELEGANT (DE)** is a multi-tenant SaaS Garments Factory Management and Accounting ERP platform built for Bangladesh businesses (default language Bengali `bn`, secondary English `en`, currency `BDT ৳`, timezone `Asia/Dhaka`).

## Package Structure (`app/src/main/java/com/example/`)
- `model/ErpModels.kt`: Firestore-aligned `@Keep @IgnoreExtraProperties` entities (`UserProfile`, `Company`, `CompanyMember`, `FinancialTransaction`, `ProductionOrder`, `TenantNotification`, `AuditLog`, `PaymentRequest`) and RBAC `UserRole` / `Permission` matrices.
- `localization/AppStrings.kt` & `BdtFormatter.kt`: Bilingual Bengali/English strings and Bangladeshi Taka (`৳`) digit grouping + `Asia/Dhaka` date formatting.
- `security/PinSecurityManager.kt`: Salted `SHA-256` Security PIN storage per authenticated `uid` using Jetpack DataStore Preferences (no plaintext PINs stored).
- `firebase/GoogleAuthHelper.kt`: Jetpack `CredentialManager` (`GetSignInWithGoogleOption` + `GetGoogleIdOption`) Google Sign-In and `NonCancellable` sign-out.
- `firebase/ErpRepository.kt`: Cloud Firestore repository bound to `R.string.firestore_database_id` with `WriteBatch` company bootstrap, real-time `.snapshots()` flows, and `Result<T>` error handling.
- `ui/state/ErpViewModel.kt`: Unwrapped state flows, date filters, computed BDT financial KPIs, and PIN lock state machine.
- `ui/screens/` & `ui/components/`: Luxury Obsidian Black & Champagne Gold UI screens (`LandingAuthScreen`, `PinScreens`, `CompanyWizardScreen`, `DashboardScreen`, `LedgerAndProductionScreen`, `TeamAndSubscriptionScreen`, `SettingsAndAuditScreen`).
