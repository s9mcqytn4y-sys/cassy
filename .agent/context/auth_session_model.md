# Auth and Session Model

## Core posture
- Personal identity on shared device.
- Online-first login.
- Offline fallback through cached grants + local PIN + enrolled device.
- Supervisor PIN approval allowed only under policy.
- Session is terminal-scoped and audit-visible.

## Hard gates
- No valid login -> no operational access.
- No terminal binding -> no operational access.
- No valid cached grant in offline mode -> blocked.
- No valid approval evidence -> sensitive flow fails closed.

## Key entities to keep in mind
- EmployeeIdentity
- DeviceEnrollment
- LocalCredential
- AuthSession
- CachedGrantProfile
- ApprovalAuthDecision
- AuthAttempt
- OfflineOperationWindow
