# Cassy Architecture Rules

## Non-negotiables
- checkout happy path must not depend on a server roundtrip
- UI must not own business invariants
- native app-shell remains mandatory
- printer/scanner/payment/permission/lifecycle remain native concerns
- audit/outbox are business decisions, not late accessories
- one operational writer per terminal/device until a proven design replaces it

## Dependency direction
UI -> App Shell / Presenter -> Application -> Domain -> Data -> Integration Adapter

## V1 discipline
- V1 = POS core
- do not push ERP-lite breadth into V1
- do not mix prepared F&B/Service boundaries into retail shipping paths
- do not treat desktop executable as desktop retail readiness

## Refactor discipline
- no cosmetic mega-rename
- staged restructuring only
- delete/move/rename allowed, but with boundary awareness
- bulk destructive change needs a checkpoint
