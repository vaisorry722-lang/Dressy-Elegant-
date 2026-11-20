# DRESSY ELEGANT — Firebase & Multi-Tenant Security Setup

## Provisioned Resources
- **Google Services Config**: `app/google-services.json`
- **Named Firestore Database**: Defined in `app/src/main/res/values/firebase_applet_config.xml` (`R.string.firestore_database_id`).
- **Authentication**: Google Sign-In via Jetpack `CredentialManager` (`GetSignInWithGoogleOption`).
  - *Note*: Additional providers (Email/Password or Phone OTP) can be enabled in the Firebase Console under Authentication > Sign-in method if needed in future phases.

## Firestore Collections (`/firebase-blueprint.json` & `/firestore.rules`)
- `/users/{userId}` — Owner-isolated user profile and preferences.
- `/companies/{companyId}` — Multi-tenant business workspace (`memberUids`, `subscriptionTier`, `paymentNumber: "301620556052"`).
- `/companies/{companyId}/members/{memberUid}` — Authoritative RBAC membership (`COMPANY_OWNER`, `COMPANY_ADMIN`, `MANAGER`, `ACCOUNTANT`, `EMPLOYEE`, `PRODUCTION_MANAGER`, `INVENTORY_MANAGER`, `SALES_MANAGER`, `KARIGOR`, `VIEWER`). `SUPER_ADMIN` cannot be granted from client code.
- `/companies/{companyId}/transactions/{transactionId}` — BDT accounting ledger entries (`hasFinanceAccess` gated).
- `/companies/{companyId}/production_orders/{orderId}` — Garments/Burqa/Abaya production batches with terminal state locking.
- `/companies/{companyId}/notifications/{notificationId}` — Tenant alerts.
- `/companies/{companyId}/audit_logs/{logId}` — Immutable security & operational audit logs.
- `/companies/{companyId}/payment_requests/{requestId}` — Subscription payment references (`PENDING_VERIFICATION`) for server-side entitlement verification.
