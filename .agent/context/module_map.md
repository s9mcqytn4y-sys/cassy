# Module Map

## Target topology
```text
repo/
├── apps/
│   ├── android-pos/
│   │   ├── app-shell/
│   │   ├── feature-sales-ui/
│   │   ├── feature-shift-ui/
│   │   ├── feature-receipt-ui/
│   │   └── device-adapters/
│   ├── android-mobile/
│   └── desktop-backoffice/
├── shared/
│   ├── platform-core/
│   ├── kernel/{domain,application,data}
│   ├── masterdata/{domain,application,data}
│   ├── sales/{domain,application,data}
│   ├── returns/{domain,application,data}
│   ├── cash/{domain,application,data}
│   ├── inventory/{domain,application,data}
│   ├── reporting/{domain,application,data}
│   ├── sync/{domain,application,data}
│   ├── auth/{application,data}
│   └── integrations/{hqapi,payment,identity,printer}
└── backend/
```

## Dependency rules
- UI depends on app-shell + application contracts.
- Application depends on its own domain, shared kernel, and explicit ports/facades.
- Domain depends only on shared kernel domain types and pure Kotlin utilities.
- Data is the only place allowed to map SQLDelight rows, DTOs, and provider payloads.
