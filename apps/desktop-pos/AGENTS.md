# Cassy Desktop Presentation Agent Guidance

## Focus
- **Desktop-First**: This is the primary outlet for Cassy R5.
- **UI Architecture**: Compose Multiplatform.
- **Reporting/Admin**: Implementation of reconciliation flows and summary views.

## Hardened UI Rules
- Use `CassyCurrencyInput` for all monetary fields.
- Sidebar must be a Slim Rail (72dp).
- Mandatory `CassySafetyDialog` for critical operations (End Shift, Close Day).
- Keyboard shortcuts (F11, F12) must be operational for shift/day closure.

## Truth & Consistency
- Presentation must reflect the state from `shared` modules exactly.
- Do not bypass `shared` domain logic for reporting.
- Ensure "Pending/Blocked" states are visible to the user as per R5 visibility goals.
