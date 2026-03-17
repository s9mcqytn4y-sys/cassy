# UI/UX & Interaction Rules

## Screen Fit & Layout
- Standard application start is **Maximized** (`WindowPlacement.Maximized`).
- Sidebar is fixed at **72dp** (Slim Rail).
- Right Cart Panel is fixed at **380dp**.

## Feedback & Validation
- All `UiBanner` (toasts) MUST auto-dismiss after **3000ms**.
- Feedback must not contain technical IDs or ambiguous error codes.
- Use `CassyErrorFeedback` for inline validation directly under the source field.

## Safety & Operations
- Critical actions (**End Shift**, **Close Day**) MUST use `CassySafetyDialog`.
- Warnings must be human-readable (Indonesian):
  - "Tutup Shift" -> Untuk pergantian kasir.
  - "Tutup Hari" -> HANYA saat toko benar-benar tutup.

## Ergonomics & Speed
- `F1/F5`: Sync/Refresh.
- `F11/F12`: End Shift / Close Day.
- `NumPadEnter`: Fast submit for currency inputs.
- Currency inputs must be right-aligned and auto-formatted.

## Terminology (No Jargon)
- **ID Terminal**: Identitas komputer ini.
- **Buka Kasir**: Memulai shift baru.
- **Modal Awal**: Uang kas saat ini.
