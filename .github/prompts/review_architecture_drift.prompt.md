# Review architecture drift

Inspect the changed files and answer:
1. Which bounded context owns this change?
2. Does any UI/app-shell code leak business rules or SQL?
3. Is a legacy bridge being extended instead of reduced?
4. Are audit/outbox/sync states explicit where required?
5. What concrete refactor would reduce long-term drift?
