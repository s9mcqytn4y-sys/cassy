# Cassy Auth Strategy Phase 1

## Document Overview
Prescriptive auth/session baseline untuk shared POS device dengan personal identity, fast unlock, offline fallback, dan approval-heavy posture.

## Purpose
Menetapkan identity model, factor model, device enrollment, session state, offline policy, dan approval auth decision.

## Scope
Android POS mandatory, Android Mobile selective, Desktop Backoffice selective, first login, daily unlock, offline login, step-up approval, domain model, state machine, dan risk controls.

## Key Decisions / Core Rules
Online-first with offline cached grants; device enrollment + PIN fast unlock + optional biometrics; supervisor PIN approval diperbolehkan secara terkontrol; session bersifat terminal-scoped dan shift-aware.

## Detailed Content

### Normalized Source Body
Retail POS · Offline-First · Shared Device · Personal Identity · Fast Unlock · Audit-Heavy

STATUS
- Prescriptive baseline for implementation handoff
- Scope: Android POS mandatory, Android Mobile selective, Desktop Backoffice selective
- Primary runtime posture: local-first operational continuity, authority-convergent to HQ
- Identity model: personal employee identity on shared POS device
- Approval model: local supervisor PIN allowed under policy
- Session model: terminal-scoped, shift-aware, audit-visible

## Traceability Base
- Cassy architecture already fixes these constraints:
1) local DB is immediate operational source of truth,
2) sync is explicit durable concern,
3) auth is online-first with offline cached grants,
4) supervisor PIN approval is allowed offline in controlled mode,
5) app-shell owns session bootstrap, permission gates, connectivity state, and terminal binding,
6) UI stays native, business logic lives in shared KMP layers.
- E2E UX baseline already says:
1) login is mandatory before operations,
2) terminal binding is a hard gate,
3) guided dashboard is the main entry,
4) offline is controlled degradation, not bypass,
5) approval without audit evidence is forbidden.
- QA baseline already marks UC-38 Auth & Validate Role and UC-39 Audit Log as P1, explicitly offline-aware, and requires verification of employee role/pin state, audit_log, outbox_event, retry, and reconciliation state.

======================================================================
## 1. GOALS
======================================================================

1. Keep cashier unlock fast.
2. Keep identity personal even on shared device.
3. Allow offline operation for safe flows.
4. Prevent silent privilege escalation.
5. Make sensitive operations step-up authenticated.
6. Make every critical auth/approval decision auditable and syncable.
7. Prevent brute-force PIN abuse on store devices.
8. Make recovery workable in real store operations, not consumer-app fantasy.

======================================================================
## 2. NON-NEGOTIABLE DESIGN DECISIONS
======================================================================

### 2.1 Identity
- One employee = one personal identity.
- Never use one shared cashier account for a whole store.
- Owner may hold multiple capabilities, but capability is still evaluated per user + device + terminal + store + business-day context.

### 2.2 Device model
- One operational POS writer per terminal/device in phase 1.
- Terminal is a first-class context in auth/session and numbering scope.
- Device must be enrolled and terminal-bound before it can be used operationally.

### 2.3 Auth posture
- Primary login: online-first against HQ auth service.
- Offline fallback: constrained cached grants + local PIN + device-bound enrollment secret.
- Sensitive approvals: local supervisor PIN allowed when policy permits.

### 2.4 UX posture
- No free-navigation home for critical flow.
- User lands in Guided Operations Dashboard after successful auth.
- If auth, device binding, or policy preconditions fail, user stays blocked with explicit reason and explicit next action.

======================================================================
## 3. AUTH FACTOR MODEL
======================================================================

Factor A - Account Password
- Used for:
- first login on device
- forced re-auth after severe lockout
- credential rotation / reset completion
- Not used for frequent cashier unlock

Factor B - Device Enrollment Secret
- Issued only after successful online login
- Bound to device_id + terminal_id + store_id + employee scope or scoped grant profile
- Stored only encrypted at rest

Factor C - 6-digit PIN
- Daily fast unlock
- Offline unlock
- Step-up auth for selected sensitive actions
- Local supervisor approval path

Factor D - Biometrics
- Optional
- Used only as cryptographic gate to unlock secret/session, not as a fake boolean shortcut
- Allowed for:
- fast unlock after prior enrollment
- step-up auth for critical operations
- Never the only recovery factor
Android implementation should use system BiometricPrompt, can authenticate with a CryptoObject, and requires USE_BIOMETRIC permission.
OWASP also recommends additional authentication for sensitive in-app actions rather than relying on a single base login.

======================================================================
## 4. PRIMARY FLOWS
======================================================================

### 4.1 First Login on New POS Device
Happy path:
## 1. App launch
2. Bootstrap reads app config, terminal binding state, local snapshot state, sync health, device health
## 3. If terminal not bound -> go to Terminal Bind / Device Setup
## 4. User enters username/email + password
## 5. Server authenticates user
## 6. Server returns:
- employee profile
- role/capability grants
- offline policy profile
- session token
- refresh token or device enrollment challenge
## 7. Device generates hardware-protected key material
## 8. Device completes enrollment
## 9. App requires PIN setup
## 10. App offers biometric enablement
## 11. App stores encrypted local auth cache
## 12. User lands on Guided Operations Dashboard

### 4.2 Daily Unlock
Happy path:
## 1. User selects account on lock screen or last active user is suggested
## 2. User enters 6-digit PIN OR uses biometric
## 3. App verifies locally
## 4. Session resumed
## 5. Guided dashboard or previous safe state restored

### 4.3 Offline Login
Happy path:
## 1. User opens app with no connectivity
## 2. App checks:
- device enrolled?
- terminal bound?
- offline window still valid?
- cached grants still valid?
- local employee credential exists?
## 3. User enters PIN
## 4. App verifies PIN locally
## 5. App decrypts offline session profile / cached grant package
## 6. App creates local session with OFFLINE mode marker
## 7. User proceeds only into policy-allowed flows

### 4.4 Step-up Approval
Happy path:
## 1. Cashier initiates sensitive flow
## 2. Policy engine decides APPROVAL_REQUIRED
## 3. Supervisor approval sheet opens
## 4. Supervisor identifies self
## 5. Supervisor authenticates using local PIN or biometric if policy allows
## 6. Reason code is mandatory
## 7. Approval decision recorded
## 8. Business flow continues or is rejected

======================================================================
## 5. DOMAIN MODEL
======================================================================

### 5.1 Aggregate / Entity List

A. EmployeeIdentity
- employeeId
- employeeCode
- displayName
- status
- storeScope
- roleAssignments
- capabilitySnapshotVersion
- serverVersion
- lastSyncedAt

B. DeviceEnrollment
- enrollmentId
- deviceId
- terminalId
- storeId
- status
- enrolledByEmployeeId
- enrolledAt
- revokedAt
- publicKeyId
- encryptedEnrollmentSecret
- offlinePolicyVersion
- grantSnapshotVersion
- lastValidationAt

C. LocalCredential
- credentialId
- employeeId
- deviceId
- pinHash
- pinSalt
- pinKdfParams
- failedAttemptCount
- softLockedUntil
- hardLockedUntil
- passwordReauthRequired
- biometricEnabled
- biometricBindingState
- lastPinChangedAt
- offlineAuthExpiresAt
- cachedGrantExpiresAt
- status

D. AuthSession
- sessionId
- employeeId
- deviceId
- terminalId
- storeId
- businessDayId nullable
- shiftId nullable
- authMode = ONLINE | OFFLINE
- unlockMethod = PASSWORD | PIN | BIOMETRIC | SUPERVISOR_APPROVAL
- status = ACTIVE | LOCKED | EXPIRED | TERMINATED
- createdAt
- lastActivityAt
- idleExpiresAt
- hardExpiresAt
- offlineWindowId nullable
- correlationId

E. CachedGrantProfile
- profileId
- employeeId
- deviceId
- capabilitySetHash
- roleSnapshot
- policySnapshotVersion
- issuedAt
- expiresAt
- sourceSessionId
- signatureState
- status

F. ApprovalAuthDecision
- approvalId
- contextType
- contextId
- operationType
- requesterEmployeeId
- approverEmployeeId
- authMethod
- reasonCode
- reasonNote
- decision = APPROVED | REJECTED
- decisionAt
- offlineFlag
- syncState

G. AuthAttempt
- attemptId
- employeeId nullable
- deviceId
- terminalId
- attemptType = LOGIN_PASSWORD | UNLOCK_PIN | UNLOCK_BIOMETRIC | APPROVAL_PIN | RESET
- result = SUCCESS | FAIL | BLOCKED | THROTTLED
- failureReason = INVALID_PIN | INVALID_PASSWORD | OFFLINE_NOT_ALLOWED | EXPIRED_CACHE | DEVICE_REVOKED | TOO_MANY_ATTEMPTS | CAPABILITY_DENIED | TERMINAL_MISMATCH
- occurredAt
- correlationId

H. PasswordResetRequest
- resetId
- targetEmployeeId
- requestedByEmployeeId
- approvedByEmployeeId nullable
- resetType = SUPERVISOR_ASSISTED | HQ_ASSISTED
- status = REQUESTED | APPROVED | COMPLETED | REJECTED | EXPIRED
- temporarySecretIssued
- reasonCode
- createdAt
- completedAt

I. OfflineOperationWindow
- windowId
- openedAt
- lastConnectivityAt
- closeRequestedAt nullable
- status = OPEN | RECONCILING | CLOSED | BLOCKED
- reason
- criticalSyncBacklogFlag
- authPolicyTightenedFlag

### 5.2 Value Objects
- DeviceId
- TerminalId
- StoreId
- EmployeeId
- SessionId
- CapabilityCode
- ReasonCode
- PinHashSpec
- GrantExpiry
- AuthMode
- UnlockMethod
- LockState
- PolicyDecision

======================================================================
## 6. STATE MACHINES
======================================================================

### 6.1 DeviceEnrollment.status
- PENDING_BIND
- BOUND
- ACTIVE
- SUSPENDED
- REVOKED
- REENROLL_REQUIRED

Rules:
- Device without ACTIVE enrollment cannot enter operational flow
- REVOKED must hard-block auth
- SUSPENDED may allow readonly diagnostics only if policy allows

### 6.2 LocalCredential.status
- ACTIVE
- SOFT_LOCKED
- HARD_LOCKED
- REAUTH_REQUIRED
- EXPIRED
- REVOKED

### 6.3 AuthSession.status
- ACTIVE
- IDLE_LOCKED
- HARD_EXPIRED
- TERMINATED
- OFFLINE_RESTRICTED

### 6.4 CachedGrantProfile.status
- VALID
- STALE_ALLOWED
- EXPIRED
- INVALID_SIGNATURE
- REVOKED

### 6.5 ApprovalAuthDecision.syncState
- LOCAL_ONLY
- QUEUED
- SYNCED
- CONFLICTED
- REJECTED_BY_HQ

======================================================================
## 7. BUSINESS RULES
======================================================================

BR-01
Personal identity is mandatory. Shared store account is forbidden.

BR-02
Without valid login there is no operational access. This is already aligned with the guided E2E gate.

BR-03
Without terminal binding there is no operational access. User must be sent to setup/bind flow.

BR-04
Offline auth is not a bypass. It is controlled degradation only. Flows allowed offline must remain policy-scoped and audit-visible.

BR-05
Offline unlock requires all of:
- ACTIVE device enrollment
- non-expired local credential
- non-expired cached grants
- non-expired offline window
- valid PIN or biometric-unlocked secret

BR-06
PIN length = 6 digits.

BR-07
PIN may be numeric-only for cashier speed.

BR-08
Biometric is optional, never mandatory, never sole recovery path.

BR-09
Sensitive operations must trigger additional authentication or approval. This matches OWASP guidance for step-up auth on sensitive actions.

BR-10
Supervisor approval offline is allowed only if local supervisor credential and policy snapshot are valid. Cassy docs already permit local PIN approval under policy.

BR-11
Approval without explicit reason code is invalid.

BR-12
All sensitive auth and approval decisions must produce audit state and, when needed, outbox/sync state. Cassy explicitly treats audit as append-only and part of the local business decision.

BR-13
If audit write fails, sensitive flow must not silently succeed.

BR-14
Owner is not a magical bypass. Owner still must leave approval/audit evidence.

BR-15
Role title alone is not enough. Capability + policy + store/terminal/business-day context decide access.

BR-16
Application boundary, not UI, is where role guard is enforced. QA baseline explicitly requires role guard to be evaluated at application boundary.

BR-17
Cached grant profile must be scoped and time-limited.

BR-18
Offline login maximum validity window = 72 hours from last successful online validation.
Note: this is your chosen policy. It is a design decision, not something dictated by the docs.

BR-19
After severe lockout, password re-auth is required.

BR-20
No destructive overwrite of auth history. Auth attempts, approval decisions, and audit records are append-only.

======================================================================
## 8. BRUTE FORCE DEFENSE
======================================================================

### 8.1 Attempt Ladder
- Attempts 1-4: normal
- Attempt 5: throttle 30 seconds
- Attempt 6: throttle 60 seconds
- Attempt 7: throttle 5 minutes
- Attempt 8: throttle 15 minutes
- Attempt 9: throttle 30 minutes
- Attempt 10: hard lock local credential
- Attempt 15 cumulative in rolling window: password re-auth required

### 8.2 Additional Controls
- Failed attempts tied to employee_id + device_id + terminal_id
- Reset failedAttemptCount only on successful auth
- Biometric failure does NOT bypass PIN lock state
- If device enrollment revoked, biometric unlock must fail closed
- Supervisor cannot unlock another user merely by viewing UI; explicit approval auth is required

### 8.3 Anti-automation Notes
- No per-keystroke remote dependency
- No server-side brute-force dependency for offline unlock
- Store local event for every throttled/blocked attempt
- Sync blocked attempts later to HQ

======================================================================
## 9. SESSION POLICY
======================================================================

### 9.1 Session Timers
- Idle timeout = 5 minutes
- Hard session timeout = 12 hours
- Shift-bound session = encouraged
- Require re-unlock on app foreground after idle timeout
- Require full re-auth after hard timeout or policy refresh invalidation

### 9.2 Session Scope
- Session is terminal-scoped
- Session is device-scoped
- Session is store-scoped
- Session is optionally linked to active business_day and shift

### 9.3 Session Transitions
- login success -> ACTIVE
- idle timeout -> IDLE_LOCKED
- user logout -> TERMINATED
- device revoke -> TERMINATED
- policy invalidation -> REAUTH_REQUIRED
- offline expiry hit -> OFFLINE_RESTRICTED

======================================================================
## 10. OFFLINE AUTH POLICY
======================================================================

### 10.1 Allowed Offline
- unlock existing enrolled user
- open shift if offline auth/cache policy allows
- standard cash sale
- receipt preview/issue/reprint
- product lookup using valid local master snapshot
- close shift locally if policy allows
This matches the E2E and architecture offline baseline.

### 10.2 Restricted / Conditional Offline
- return/refund
- inventory adjustments above threshold
- sensitive approvals without valid local supervisor credential
- business-day close if minimum readiness not satisfied
The Cassy flow docs already classify these as restricted/conditional.

### 10.3 Hard Offline Blocks
- device not enrolled
- terminal mismatch
- cached grants expired
- offline window expired
- policy snapshot absent for required flow
- account revoked
- hard-locked credential

======================================================================
## 11. FORGOT PASSWORD / RECOVERY
======================================================================

### 11.1 Phase-1 Supported Recovery
A. Supervisor-assisted reset
B. HQ-assisted reset

### 11.2 Supervisor-assisted reset
Allowed only if:
- policy allows
- supervisor capability present
- supervisor authenticates successfully
- reason code entered
- reset event recorded
- target user must still complete online password refresh within policy window

### 11.3 HQ-assisted reset
Used when:
- supervisor unavailable
- local policy disallows reset
- user account has higher risk flags
- credential compromise suspected

### 11.4 Explicit Rejections
- No email-only self-service recovery baseline for phase 1 POS
- No insecure "security question" flow
- No silent reset without audit trail

======================================================================
## 12. BIOMETRIC INTEGRATION
======================================================================

### 12.1 Android
- Use androidx.biometric BiometricPrompt
- Prefer CryptoObject path for unlocking encrypted session/enrollment material
- USE_BIOMETRIC is the platform permission; Android classifies it as a normal permission
- Support fallback to PIN when biometric unavailable or fails
- Keep biometric prompt in native layer/app shell
Android's official docs say BiometricPrompt is the system-provided biometric prompt, supports authenticate(info, crypto), and USE_BIOMETRIC is the relevant permission.

### 12.2 iOS
- Use LocalAuthentication / LAContext
- Protect secrets via Keychain access control, not boolean-only "if authenticated" logic
- If Face ID is used, app must include NSFaceIDUsageDescription in Info.plist
OWASP warns that LocalAuthentication alone is not secure storage and should be paired with Keychain access control for sensitive secrets; Apple's plist reference documents NSFaceIDUsageDescription for Face ID usage.

### 12.3 Cassy-specific rule
- Biometric does not replace password enrollment
- Biometric does not replace local PIN entirely
- Biometric only unlocks encrypted local material already provisioned by a valid prior session

======================================================================
## 13. PERMISSION / CONSENT UX
======================================================================

### 13.1 Android biometric enablement
Do NOT ask on first app launch.

Ask only after:
- successful first online login
- PIN setup complete
- device enrollment active

Prompt copy:
"Gunakan fingerprint / face unlock untuk buka sesi lebih cepat di device ini?"

If accepted:
- register biometric-bound key alias
- store biometricEnabled = true

If declined:
- continue with PIN only

### 13.2 iOS Face ID
- Do not surface Face ID option before device supports it and enrollment state exists
- Ensure NSFaceIDUsageDescription exists before feature is reachable
Apple requires NSFaceIDUsageDescription when the app accesses Face ID on supported hardware.

### 13.3 Principle
- permission request must be contextual, not premature
- this is consistent with Cassy app-shell owning permission gates rather than shared business layers.

======================================================================
## 14. SENSITIVE OPERATIONS THAT REQUIRE STEP-UP AUTH
======================================================================

Mandatory step-up candidates for phase 1:
- shift handover conflict resolution
- opening cash override outside policy
- cash in/out above limit
- safe drop confirm
- close shift variance above tolerance
- void after policy threshold
- manual discount / price override above threshold
- return/refund requiring approval
- business-day close exception path
These flows are already flagged across the Cassy matrix as approval/auth/data-sensitive.

Step-up method order:
## 1. Supervisor PIN
## 2. Supervisor biometric if enabled and policy allows
## 3. Fallback HQ-assisted flow if local approval unavailable and operation is not offline-allowed

======================================================================
## 15. LOCAL SQLITE SCHEMA (PHASE 1 BASELINE)
======================================================================

### 15.1 Tables

TABLE employee_identity (
employee_id TEXT PRIMARY KEY,
employee_code TEXT NOT NULL,
display_name TEXT NOT NULL,
account_status TEXT NOT NULL,
store_id TEXT NOT NULL,
role_snapshot_json TEXT NOT NULL,
capability_snapshot_json TEXT NOT NULL,
capability_snapshot_version INTEGER NOT NULL,
server_version INTEGER NOT NULL DEFAULT 1,
last_synced_at TEXT,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL
);

TABLE device_enrollment (
enrollment_id TEXT PRIMARY KEY,
device_id TEXT NOT NULL,
terminal_id TEXT NOT NULL,
store_id TEXT NOT NULL,
status TEXT NOT NULL, -- PENDING_BIND | BOUND | ACTIVE | SUSPENDED | REVOKED | REENROLL_REQUIRED
enrolled_by_employee_id TEXT NOT NULL,
public_key_id TEXT NOT NULL,
encrypted_enrollment_secret BLOB NOT NULL,
offline_policy_version INTEGER NOT NULL,
grant_snapshot_version INTEGER NOT NULL,
last_validation_at TEXT,
enrolled_at TEXT NOT NULL,
revoked_at TEXT,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL
);

CREATE UNIQUE INDEX uq_device_enrollment_active
ON device_enrollment(device_id, terminal_id, store_id)
WHERE status IN ('BOUND', 'ACTIVE');

TABLE local_credential (
credential_id TEXT PRIMARY KEY,
employee_id TEXT NOT NULL,
device_id TEXT NOT NULL,
pin_hash BLOB NOT NULL,
pin_salt BLOB NOT NULL,
pin_kdf_params_json TEXT NOT NULL,
failed_attempt_count INTEGER NOT NULL DEFAULT 0,
soft_locked_until TEXT,
hard_locked_until TEXT,
password_reauth_required INTEGER NOT NULL DEFAULT 0,
biometric_enabled INTEGER NOT NULL DEFAULT 0,
biometric_binding_state TEXT NOT NULL DEFAULT 'DISABLED', -- DISABLED | ENROLLED | INVALIDATED
offline_auth_expires_at TEXT NOT NULL,
cached_grant_expires_at TEXT NOT NULL,
status TEXT NOT NULL, -- ACTIVE | SOFT_LOCKED | HARD_LOCKED | REAUTH_REQUIRED | EXPIRED | REVOKED
last_pin_changed_at TEXT NOT NULL,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL,
FOREIGN KEY(employee_id) REFERENCES employee_identity(employee_id)
);

CREATE UNIQUE INDEX uq_local_credential_employee_device
ON local_credential(employee_id, device_id);

TABLE cached_grant_profile (
profile_id TEXT PRIMARY KEY,
employee_id TEXT NOT NULL,
device_id TEXT NOT NULL,
capability_set_hash TEXT NOT NULL,
role_snapshot_json TEXT NOT NULL,
policy_snapshot_version INTEGER NOT NULL,
issued_at TEXT NOT NULL,
expires_at TEXT NOT NULL,
source_session_id TEXT,
signature_state TEXT NOT NULL, -- VALID | INVALID | UNKNOWN
status TEXT NOT NULL, -- VALID | STALE_ALLOWED | EXPIRED | INVALID_SIGNATURE | REVOKED
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL,
FOREIGN KEY(employee_id) REFERENCES employee_identity(employee_id)
);

CREATE INDEX ix_cached_grant_profile_lookup
ON cached_grant_profile(employee_id, device_id, status, expires_at);

TABLE auth_session (
session_id TEXT PRIMARY KEY,
employee_id TEXT NOT NULL,
device_id TEXT NOT NULL,
terminal_id TEXT NOT NULL,
store_id TEXT NOT NULL,
business_day_id TEXT,
shift_id TEXT,
auth_mode TEXT NOT NULL, -- ONLINE | OFFLINE
unlock_method TEXT NOT NULL, -- PASSWORD | PIN | BIOMETRIC | SUPERVISOR_APPROVAL
status TEXT NOT NULL, -- ACTIVE | LOCKED | EXPIRED | TERMINATED | OFFLINE_RESTRICTED
idle_expires_at TEXT NOT NULL,
hard_expires_at TEXT NOT NULL,
last_activity_at TEXT NOT NULL,
correlation_id TEXT NOT NULL,
offline_window_id TEXT,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL
);

CREATE INDEX ix_auth_session_active
ON auth_session(employee_id, device_id, terminal_id, status);

TABLE auth_attempt (
attempt_id TEXT PRIMARY KEY,
employee_id TEXT,
device_id TEXT NOT NULL,
terminal_id TEXT NOT NULL,
attempt_type TEXT NOT NULL,
result TEXT NOT NULL,
failure_reason TEXT,
occurred_at TEXT NOT NULL,
correlation_id TEXT NOT NULL,
created_at TEXT NOT NULL
);

CREATE INDEX ix_auth_attempt_lookup
ON auth_attempt(device_id, terminal_id, occurred_at DESC);

TABLE approval_auth_decision (
approval_id TEXT PRIMARY KEY,
context_type TEXT NOT NULL,
context_id TEXT NOT NULL,
operation_type TEXT NOT NULL,
requester_employee_id TEXT NOT NULL,
approver_employee_id TEXT NOT NULL,
auth_method TEXT NOT NULL, -- PIN | BIOMETRIC
reason_code TEXT NOT NULL,
reason_note TEXT,
decision TEXT NOT NULL, -- APPROVED | REJECTED
decision_at TEXT NOT NULL,
offline_flag INTEGER NOT NULL DEFAULT 0,
sync_state TEXT NOT NULL DEFAULT 'LOCAL_ONLY', -- LOCAL_ONLY | QUEUED | SYNCED | CONFLICTED | REJECTED_BY_HQ
correlation_id TEXT NOT NULL,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL
);

CREATE INDEX ix_approval_context
ON approval_auth_decision(context_type, context_id, decision_at DESC);

TABLE password_reset_request (
reset_id TEXT PRIMARY KEY,
target_employee_id TEXT NOT NULL,
requested_by_employee_id TEXT NOT NULL,
approved_by_employee_id TEXT,
reset_type TEXT NOT NULL, -- SUPERVISOR_ASSISTED | HQ_ASSISTED
status TEXT NOT NULL, -- REQUESTED | APPROVED | COMPLETED | REJECTED | EXPIRED
temporary_secret_issued INTEGER NOT NULL DEFAULT 0,
reason_code TEXT NOT NULL,
created_at TEXT NOT NULL,
completed_at TEXT,
updated_at TEXT NOT NULL
);

TABLE offline_operation_window (
window_id TEXT PRIMARY KEY,
opened_at TEXT NOT NULL,
last_connectivity_at TEXT,
close_requested_at TEXT,
status TEXT NOT NULL, -- OPEN | RECONCILING | CLOSED | BLOCKED
reason TEXT,
critical_sync_backlog_flag INTEGER NOT NULL DEFAULT 0,
auth_policy_tightened_flag INTEGER NOT NULL DEFAULT 0,
created_at TEXT NOT NULL,
updated_at TEXT NOT NULL
);

### 15.2 Cross-cutting tables used by auth bundle
- audit_log
- outbox_event
- sync_batch
- sync_item
- sync_conflict
These are already target-state Cassy entities and should be reused rather than reinvented.

### 15.3 Mandatory transactional bundles
A. login success bundle
- auth_session
- auth_attempt(success)
- audit_log
- optional outbox_event

B. failed unlock bundle
- local_credential.failed_attempt_count update
- auth_attempt(fail)
- audit_log optional by policy

C. approval decision bundle
- approval_auth_decision
- business decision context mutation
- audit_log
- outbox_event
Cassy architecture requires local business writes, audit, and outbox to be treated atomically when they belong to one business decision.

======================================================================
## 16. HQ / POSTGRES BASELINE
======================================================================

Core HQ auth tables:
- hq_employee_identity
- hq_role_assignment
- hq_capability_policy
- hq_device_enrollment
- hq_cached_grant_issue_log
- hq_auth_revocation
- hq_password_reset_ticket
- hq_auth_event_ingest
- hq_approval_decision
- hq_audit_log

Purpose:
- authoritative employee and policy management
- revocation and rotation authority
- device enrollment validation
- conflict/reconciliation handling for auth events
- central reporting of suspicious auth activity

======================================================================
## 17. API CONTRACT OUTLINE
======================================================================

POST /auth/login
Request:
- identifier
- password
- device_id
- terminal_id
- store_id
- app_version
- platform
Response:
- access_token
- refresh_token
- employee_profile
- capability_snapshot
- offline_policy_profile
- enrollment_challenge or enrollment_status
- cache_ttl
- force_pin_setup flag
- biometric_allowed flag

POST /auth/enroll-device
Request:
- access_token
- device_id
- terminal_id
- public_key
- app_attestation placeholder future
Response:
- enrollment_id
- encrypted_enrollment_secret
- offline_policy_version
- grant_snapshot_version

POST /auth/refresh
Request:
- refresh_token
- device_id
Response:
- rotated access token
- rotated refresh token
- updated capability snapshot
- revoked flag if any

GET /auth/permissions
Response:
- capability set
- policy version
- offline allowed flows
Architecture doc already lists /auth/login, /auth/refresh, and /auth/permissions as the API category for authentication and session.

POST /auth/reset/request
POST /auth/reset/approve
POST /auth/reset/complete

POST /sync/batches
- must carry auth-related audit/approval events if queued offline

======================================================================
## 18. MODULE / LAYER PLACEMENT
======================================================================

apps/android-pos/app-shell
- bootstrap
- terminal binding gate
- permission gate
- biometric prompt launcher
- session restoration

apps/android-pos/feature-auth-ui
- login screen
- PIN setup
- PIN unlock
- user switch
- forgot password request
- access denied

apps/android-mobile/feature-approval-ui
- supervisor approval assist (selective)

shared/auth/application
- LoginUseCase
- UnlockSessionUseCase
- VerifyOfflineGrantUseCase
- SetupPinUseCase
- ChangePinUseCase
- EnableBiometricUseCase
- RequireStepUpAuthUseCase
- SupervisorApprovalAuthUseCase
- ForgotPasswordUseCase
- RevokeLocalSessionUseCase

shared/auth/data
- AuthRepository
- CredentialStore
- DeviceEnrollmentStore
- SessionStore
- GrantCacheStore
- AuthAuditWriter
- AuthSyncAdapter

integrations/identity
- HQ auth API adapter

This placement matches Cassy's prescribed split: app-shell owns permission/session bootstrap, shared auth owns application/data, UI remains native.

======================================================================
## 19. UI / UX SCREEN MAP
======================================================================

Launch & Access
- Splash / Bootstrap
- Terminal Bind / Device Setup
- Login
- PIN Setup
- Enable Biometric Prompt
- Access Denied / Offline Auth Decision
- Session Locked

Guided Entry
- Guided Operations Dashboard
- Readiness Detail
- Sync Status Detail
- Device Diagnostics

Sensitive Approval
- Supervisor Approval Sheet
- Reason Code Picker
- Approval Result Sheet

Recovery
- Forgot Password Request
- Supervisor-assisted Reset
- HQ Reset Pending Screen

These screens fit the existing Cassy screen map and guided-operations posture.

======================================================================
## 20. UX COPY / STATE RULES
======================================================================

Use explicit operational language:
- "Login ditolak: device ini belum terhubung ke terminal."
- "Login offline ditolak: grant lokal sudah kedaluwarsa."
- "Approval supervisor diperlukan untuk melanjutkan."
- "Akses dibatasi: mode offline hanya mengizinkan operasi tertentu."
- "PIN salah. Coba lagi dalam 5 menit."
- "Biometric gagal. Gunakan PIN."
- "Password reset menunggu supervisor."

Avoid:
- "Oops"
- "Unknown issue"
- "Try later"
This is aligned with Cassy's mandatory UX state language discipline.

======================================================================
## 21. HAPPY PATH
======================================================================

Happy Path A - First setup
## 1. Launch
## 2. Terminal bind valid
## 3. Login online
## 4. Device enrolled
## 5. Set PIN
## 6. Enable biometric optional
## 7. Dashboard shown
## 8. Start business day / shift flow

Happy Path B - Daily cashier return
## 1. Launch
## 2. Last user shown
## 3. PIN unlock
## 4. Session restored
## 5. Dashboard
## 6. Continue shift or sales

Happy Path C - Sensitive approval
## 1. Cashier triggers restricted action
## 2. Approval sheet opens
## 3. Supervisor PIN auth success
## 4. Reason code submitted
## 5. Action continues
## 6. Approval + audit persisted

======================================================================
## 22. WORST CASE FLOWS
======================================================================

### 22.1 Invalid credential
- user remains blocked
- no operational entry
- auth_attempt recorded

### 22.2 Offline credential expired
- no offline login
- show "Login online diperlukan"
- readonly diagnostics only if policy allows

### 22.3 Device revoked from HQ
- next online handshake terminates session
- local credential status -> REVOKED
- future offline login blocked

### 22.4 Shift conflict right after login
- dashboard state = Blocked
- user can't jump into sales
- guided next action = supervisor handling
This aligns with existing guided E2E worst-case journey.

### 22.5 Biometric unavailable after OS change
- biometricBindingState -> INVALIDATED
- PIN required
- app offers re-enable biometric after successful PIN login

### 22.6 Brute-force attack
- throttling ladder applied
- hard lock
- password re-auth required
- repeated severe failures sync to HQ fraud monitoring

### 22.7 Approval attempted without local supervisor policy snapshot
- hard stop
- no bypass
- instruct user to go online or use HQ-assisted route

### 22.8 Audit storage unavailable
- sensitive flow must fail closed or queue atomically per policy
- never silently succeed without evidence
Cassy QA explicitly treats audit fallback as a critical risk.

======================================================================
## 23. SECURITY RULES
======================================================================

- Password is never stored locally in plaintext or reversible form.
- PIN is never stored plaintext.
- Enrollment secret is always encrypted at rest.
- Biometric only unlocks cryptographic material; it does not become app logic's sole truth signal.
- Local retries and sync retries must be idempotent.
- Audit log is append-only.
- Approval evidence is append-only.
- UI cannot decide authorization alone.
- Shared code cannot call platform biometric APIs directly; use ports/adapters.
- All auth-related writes that define one business decision must commit atomically with audit/outbox when applicable.

======================================================================
## 24. TEST BASELINE
======================================================================

P1 mandatory auth scenarios:
## 1. online login success
## 2. online login invalid password
## 3. offline login allowed with valid cached grant
## 4. offline login denied with expired cached grant
## 5. local PIN throttling ladder works
## 6. hard lock after repeated failures
## 7. password re-auth forced after severe lockout
## 8. biometric unlock success
## 9. biometric invalidated -> fallback PIN
## 10. supervisor approval offline allowed path
## 11. supervisor approval rejected path
## 12. device revoked -> local session terminated
## 13. audit/outbox atomicity on approval flow
## 14. auth policy update invalidates stale local grant
## 15. terminal mismatch blocks access

QA philosophy already requires:
- verify output + domain state + DB state + audit/outbox/sync state,
- test auth fallback explicitly,
- test role guard at application boundary,
- test offline/sync as first-class dimension.

Evidence to verify:
- employee_identity
- local_credential
- cached_grant_profile
- auth_session
- auth_attempt
- approval_auth_decision
- audit_log
- outbox_event
- sync_batch / sync_item / sync_conflict if offline queueing happened

======================================================================
## 25. EXPLICIT PHASE-1 LIMITATIONS
======================================================================

Not included yet:
- MFA mandatory for all users
- device attestation enforcement
- remote wipe
- behavioral biometrics
- passkey / WebAuthn
- peer-to-peer device trust
- multi-writer terminal sharing
- autonomous self-service password reset by email

Future phase candidates:
- hardware attestation
- risk scoring
- anomaly detection
- server-driven forced logout
- stronger offline grant signature verification
- passkey for manager/admin roles

======================================================================
## 26. FINAL RECOMMENDATION SUMMARY
======================================================================

## Final Choice
- Personal employee identity: YES
- Password for first online login: YES
- Device enrollment mandatory: YES
- Daily unlock with 6-digit PIN: YES
- Biometric optional for unlock and critical action: YES
- Offline auth using cached grants + local PIN + enrolled device: YES
- Offline auth validity window: 72 hours
- Brute-force defense: throttle + hard lock + password re-auth
- Forgot password phase 1: supervisor-assisted + HQ-assisted
- Shared device fast user switch: YES
- Supervisor PIN offline approval under policy: YES
- Guided dashboard as post-login entry point: YES

## Short Verdict
Ini desain yang masuk akal untuk Phase-1 Cassy:
- aman, tapi tidak sok enterprise berlebihan,
- cepat untuk kasir,
- realistis untuk toko offline-first,
- audit-friendly,
- dan tidak merusak boundary arsitektur Cassy yang sudah Anda tetapkan.


## Constraints / Policies
Tidak boleh ada silent privilege escalation, shared cashier account, atau approval tanpa audit evidence.

## Technical Notes
Dokumen ini harus dibaca bersama auth-related use case, UI contract, E2E flow, architecture, dan test baseline.

## Dependencies / Related Documents
- `cassy_architecture_specification_v1.md`
- `cassy_theming_ui_contract_phase_1.md`
- `cassy_e2e_store_operation_uiux_flow_scheme_v2.md`
- `store_pos_test_specification.md`
- `cassy_test_automation_specification.md`

## Risks / Gaps / Ambiguities
- Tidak ditemukan gap fatal saat ekstraksi. Tetap review ulang bagian tabel/angka jika dokumen ini akan dijadikan baseline implementasi final.

## Reviewer Notes
- Struktur telah dinormalisasi ke Markdown yang konsisten dan siap dipakai untuk design review / engineering handoff.
- Istilah utama dipertahankan sedekat mungkin dengan dokumen sumber untuk menjaga traceability lintas artefak.
- Bila ada konflik antar dokumen, prioritaskan source of truth, artefak yang paling preskriptif, dan baseline yang paling implementable.

## Source Mapping
- Original source: `CASSY-AUTH-STRATEGY-—-PHASE-1.txt` (TXT, 1159 lines)
- Output markdown: `cassy_auth_strategy_phase_1.md`
- Conversion approach: text extraction -> normalization -> structural cleanup -> standardized documentation wrapper.
- Note: marker sitasi/annotation internal non-substantif dibersihkan agar hasil markdown lebih implementable.
