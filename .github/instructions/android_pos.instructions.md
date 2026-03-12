---
applyTo: "apps/android-pos/**, apps/android/**"
---

# Android POS Instructions

- POS is the operational writer for checkout and shift-critical flows unless the task explicitly addresses a later strategic override.
- Keep scanner, printer, payment terminal callbacks, permission prompts, and lifecycle logic in native modules.
- Guided Operations Dashboard and anti-skip gates are mandatory semantics.
- Do not place SQLDelight query calls in ViewModels or Composables.
- Sales finality must survive printer failure.
- Offline mode is controlled degradation, not a bypass.
