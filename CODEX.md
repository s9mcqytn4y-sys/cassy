# Cassy Codex Entry

Use this file first when the agent is Codex or Codex-like.

## Loading order
1. `AGENTS.md`
2. `.agent/README.md`
3. `.agent/rules/architecture_rules.md`
4. `.agent/context/project_overview.md`
5. `.agent/context/bounded_contexts.md`
6. `.agent/context/module_map.md`
7. `.agent/context/critical_flows.md`
8. `.agent/context/known_repo_gaps.md`
9. `.agent/plan.md`

## Codex behavior bias
- optimize for execution clarity and correct boundaries
- prefer narrow, shippable POS-first scope over broad ERP-like expansion
- **Desktop-First**: prioritize Desktop as the primary operational and release target
- do not let checkout semantics drift across Android and Desktop
- keep device-heavy concerns native
- surface repo gaps explicitly instead of painting over them
- do not call M3/M4 done without build, test, smoke, and docs evidence
- treat Windows packaging as unproven until an actual Windows artifact is built
- keep Desktop on JDK 17 only; Java 21 drift in run/package/dev tooling is a defect
- keep configuration cache opt-in for CI, not default-on for IDE/local path
- keep stock mutation ownership inside `shared:inventory`; `shared:sales` may request inventory effects but should not own stock writes directly

## Allowed repo operations
If runtime allows it, you may:
- create/edit/move/rename/delete files and folders
- run git status/diff/add/commit/branch
- patch source code, docs, prompts, and instructions
- run build/test/lint commands relevant to the task

## Verification order
1. `.\gradlew :apps:desktop-pos:smokeRun`
2. `.\gradlew :apps:desktop-pos:run --args="--smoke-run"`
3. `.\gradlew --version`
4. `.\gradlew clean`
5. `.\gradlew build`
6. `.\gradlew test`
7. `.\gradlew detekt`
8. `.\gradlew :apps:android-pos:lintDebug`
9. `.\gradlew :apps:desktop-pos:createDistributable`
10. `.\gradlew :apps:desktop-pos:packageDistributionForCurrentOS`
11. `.\tooling\scripts\Invoke-DesktopDistributionSmoke.ps1`
