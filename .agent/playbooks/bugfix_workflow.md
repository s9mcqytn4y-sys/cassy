# Playbook — Bugfix Workflow

## Use when
- bug reproducible di Android/Desktop/shared core
- failure path checkout/history/masterdata
- suspected regression

## Steps
1. locate affected flow
2. identify violated invariant
3. confirm owner layer
4. patch at owner layer, not nearest convenient layer
5. write/update acceptance check
6. verify Android baseline
7. note parity impact on Desktop
