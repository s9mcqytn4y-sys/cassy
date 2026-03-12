# Add Test Coverage Playbook

1. Map the change to bounded context and critical flow.
2. Choose smallest useful automated layer first.
3. Verify domain state, DB state, audit state, and sync state when relevant.
4. Cover at least one alternate or failure path.
5. Add migration/FK replay checks if schema changed.
