# R5 Exception, Pending & Blocked Readback

## Readback Strategy
Operational exceptions and pending states are no longer "silent" or generic. Every non-finalized state must provide a readback that explains **What**, **Who**, **When**, and **Why**.

## Model Requirements
The `OperationalIssue` model is the carrier for these readbacks:
- **What**: `label` and `description`.
- **Who**: `actor` field (e.g., requester of an approval, or the operator who closed a shift with variance).
- **When**: `timestamp` field (converted to local terminal time).
- **Why**: `reasonCode` and `status` fields (e.g., `OPENING_CASH_EXCEPTION` or `STALLED`).

## Truthful Readback Scenarios

### 1. Pending Approvals
- **Status**: `REQUESTED`
- **Readback**: "Permintaan oleh [Requester] memerlukan tindakan supervisor."
- **Metadata**: Shows the requester name and the time the request was made.

### 2. Shift Variance
- **Status**: `COMPLETED_WITH_VARIANCE`
- **Readback**: "Ditemukan selisih kas sebesar Rp [X]."
- **Metadata**: Shows the operator who performed the closure and the exact time of closure.

### 3. Sync Latency
- **Status**: `DELAYED` or `STALLED`
- **Readback**: "Ada [N] data yang belum tersinkronisasi."
- **Metadata**: Shows the timestamp of the oldest pending event to indicate the lag start.

### 4. Hardware Failures
- **Status**: `OFFLINE` or `UNAVAILABLE`
- **Readback**: Specific detail message from the hardware port (e.g., "Kabel printer terputus").

## UI Implementation
The `OperationalIssueCard` in `apps:desktop-pos` is the standardized visual representation of this readback, ensuring that the user never has to guess why a state is blocked or pending.
