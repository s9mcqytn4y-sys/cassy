# Transaction Bundles

## Sale completion bundle
sale_transaction + sale_items + payment + receipt + cash effect (if any) + inventory ledger effect (if any) + audit + outbox

## Return bundle
return_transaction + return_lines + refund effect + stock ledger effect + approval/audit + outbox

## Shift close bundle
reconciliation + shift status update + audit + outbox

## Inventory adjustment bundle
adjustment + balance mutation + stock_ledger_entry + approval/audit + outbox

## Approval decision bundle
approval decision + reason/evidence metadata + audit + outbox when policy requires sync visibility
