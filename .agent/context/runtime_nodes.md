# Runtime Nodes

## Android POS / Tablet
Primary operational writer for checkout, receipt, shift operations, local device integration, and foreground sync visibility.

## Android Mobile
Selective approval, inventory assist, and operational status views.

## Desktop
Historically backoffice-selective in the base architecture; repo audit flags strategic pressure toward operational parity. Treat this node as parity-sensitive and not yet automatically production-ready.

## HQ API
Ingests sync, serves master data, coordinates cross-store behavior, and returns conflict/reconciliation outcomes.

## HQ Database
Authoritative consolidated persistence.
