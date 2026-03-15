# Cassy Bounded Contexts

## Active / visible now
- kernel
- masterdata
- sales

## Wider target-state
- returns
- cash
- inventory
- reporting
- sync
- auth
- integrations
- prepared boundaries: fb, service

## Rule
A context appearing in docs does not prove clean runtime ownership.
Always ask:
- who owns the data?
- who owns the invariant?
- does the critical flow still pass through legacy paths?
