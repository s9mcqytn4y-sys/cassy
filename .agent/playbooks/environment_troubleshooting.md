# Environment Troubleshooting Playbook

## Issue: Resource Busy or Locked (EBUSY)
This typically happens when VS Code (Language Support for Java by Red Hat) or another process is locking the Eclipse JDT Language Server workspace storage while an attempt is made to delete or clean it.

### Root Cause
- VS Code Language Server process (`java.exe`) is still active.
- Simultaneous build/clean from CLI and IDE.
- Indexing or file system monitoring is holding a handle on a `.tmp` index file.

### Immediate Fix (CLI)
1. **Kill Java processes**:
   ```powershell
   Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
   ```
2. **Clean VS Code Workspace Cache**:
   - Manually delete `C:\Users\Acer\AppData\Roaming\Code\User\workspaceStorage\<hash>\redhat.java\jdt_ws` if needed after stopping processes.
   - Or use VS Code command: `Java: Clean Java Language Server Workspace`.

### Prevention
- Close VS Code / Android Studio before running deep cleans of system-level storage.
- Ensure Gradle Daemons are stopped if they interfere:
  ```powershell
  ./gradlew --stop
  ```

## Issue: SQLite File Lock
If the desktop app crashes, it might hold a lock on `~/.cassy/sales.db`.
- Ensure the app is fully closed before manual DB intervention.
