# Cassy POS System

Cassy is a retail-first operational POS system built with Kotlin Multiplatform.

## Project Structure

- `apps/android-pos`: Android application for POS operations.
- `apps/desktop-pos`: Desktop application (JVM) for POS operations.
- `shared`: Shared business logic, data persistence, and bounded contexts.
- `tooling/build-logic`: Gradle convention plugins for project-wide configuration.

## Tech Stack

- **Kotlin Multiplatform**
- **Compose Multiplatform**
- **SQLDelight** for local persistence
- **Koin** for dependency injection
- **Gradle** with convention plugins

## Getting Started

To build the project:
```bash
./gradlew assemble
```

To run the desktop application:
```bash
./gradlew :apps:desktop-pos:run
```
