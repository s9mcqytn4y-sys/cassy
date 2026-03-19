# Cassy Gemini Entry (Updated 2026-03-19)

## UI Hardening Protocol (Phase 1-3)
- **Sidebar:** Slim Rail (72dp) with brand icon only.
- **Inputs:** `CassyCurrencyInput` for monetary fields with `CassyErrorFeedback`.
- **Top Bar:** System health visibility (Sync, Printer, User).
- **Cart:** Fixed Right Panel (380dp) with hardened item rows.
- **Safety:** Mandatory `CassySafetyDialog` for End Shift & Close Day.
- **Keyboard:** F1/F5 (Refresh), F11 (End Shift), F12 (Close Day).

## Working mode
- **Truthful Scope**: Prefer accurate implementation over broad ambition.
- **Desktop-First**: Desktop is the primary target.
- **Hardened Baseline**: R1, R2, dan R3 inventory truth lite sudah verified pada desktop-first lane.
- **Approval Truth**: `LIGHT_PIN` PASS, `SECOND_PIN` dan `DUAL_AUTH` belum shipped.

## Permission posture
Authorized for all repo operations including build/test/git.
