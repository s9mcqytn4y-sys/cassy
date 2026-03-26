# R5 Gradle Procedure: Verification & Hardening

Updated: 2026-03-19
Status: VERIFIED

## 1. R5 Verification Surface Inventory

| Module | Purpose | Status | Lane |
| :--- | :--- | :--- | :--- |
| `:shared:kernel` | ReportingQueryFacade & Core Models | **ACTIVE** | Common/JVM |
| `:shared:sales` | OperationalSalesPort Implementation | **ACTIVE** | Common/JVM |
| `:shared:inventory` | Stock/Ledger Truth for Reporting | **ACTIVE** | Common/JVM |
| `:apps:desktop-pos` | Reporting UI & Smoke Verification | **ACTIVE** | JVM |
| `:apps:android-pos` | Mobile Parity Surface | **PARTIAL** | Android |

## 2. Mandatory Verification Sequence

Run these commands in order to ensure R5 truth.

### Step 1: Wrapper & Structure Integrity
```powershell
.\gradlew projects
```
*Proves: Gradle daemon is alive and project structure is valid.*

### Step 2: Shared Business Logic (Fast Confidence)
```powershell
.\gradlew :shared:kernel:allTests :shared:sales:desktopTest :shared:inventory:desktopTest
```
*Proves: ReportingQueryFacade aggregates sales and inventory data correctly. Covers R5-R02 sync visibility logic.*

### Step 3: Database Migration Safety
```powershell
.\gradlew :shared:kernel:verifyCommonMainKernelDatabaseMigration :shared:inventory:verifyCommonMainInventoryDatabaseMigration
```
*Proves: Reporting-related schema changes are compatible with current DB state.*

### Step 4: Desktop Functional Smoke (R5 UI Gate)
```powershell
.\gradlew :apps:desktop-pos:smokeRun
```
*Proves: Desktop app starts, Koin injects ReportingQueryFacade, and the main dashboard can load without fatal errors.*

### Step 5: Full Build & Static Analysis
```powershell
.\gradlew build detekt
```
*Proves: Entire project compiles and adheres to R5 hardening rules (no direct SQL in UI, etc.).*

## 3. R5 Report-Test Home Mapping

| Concern | Test Owner | Status |
| :--- | :--- | :--- |
| Daily Summary Aggregation | `:shared:kernel:ReportingQueryFacadeTest` | **VERIFIED** |
| Shift Summary & Variance | `:shared:kernel:ReportingQueryFacadeTest` | **VERIFIED** |
| Sync Status & Lag Logic | `:shared:kernel:ReportingQueryFacadeTest` | **VERIFIED** |
| Sales Summary Port | `:shared:sales:SalesServiceTest` | **VERIFIED** |
| Inventory Ledger Truth | `:shared:inventory:InventoryServiceTest` | **VERIFIED** |
| Reporting UI Components | `:apps:desktop-pos` (Smoke) | **ACTIVE** |

## 4. CI/Workflow Matrix

| Workflow | Role | R5 Relevance | Status |
| :--- | :--- | :--- | :--- |
| `ci.yml` | PR Gate | Fast verification of shared logic | **STABLE** |
| `mainline.yml` | Post-Merge | Full packaging & distribution smoke | **STABLE** |
| `nightly-integrity.yml`| Scheduled | Deep DB migration & build checks | **STABLE** |
| `release-evidence.yml` | On-Demand | Generates manifest for audit | **STABLE** |
