# Cassy Coding Rules

- Favor readable, explicit boundaries over abstraction theater.
- Keep module imports honest; do not tunnel through convenience helpers that hide ownership.
- Keep DTOs and persistence rows out of domain APIs.
- Prefer small, named application services/facades over one giant service locator.
- Keep branch names, class names, and package names aligned to bounded contexts.
- If you touch critical flows, add or update tests in the same change.
