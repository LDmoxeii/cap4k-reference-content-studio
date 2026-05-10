# Reference Project Teaching Simplification Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Simplify `cap4k-reference-content-studio` so users learn from domain behavior, factories, smoke tests, and clear Gradle entry points instead of cleanup-history tests or noisy build structure.

**Architecture:** Keep the already-verified runtime, generator, snapshot, and analysis mechanisms unchanged in substance. Remove tactical-audit tests, retain behavior/factory/smoke/contract tests, reorder Gradle configuration for readability, clarify README run/generate/analysis paths, then remove temporary working docs from the repository.

**Tech Stack:** Kotlin, Spring Boot, Gradle Kotlin DSL, cap4k pipeline plugin, JUnit 5, H2

---

## File Structure

- Modify: `build.gradle.kts`
  - Reorder the root build script into a user-facing reading sequence without changing behavior.
- Modify: `README.md`
  - Clarify the three main Gradle entry paths and explain which tests users should care about.
- Modify: `README.zh-CN.md`
  - Mirror the README simplification in Chinese.
- Delete: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/BehaviorAliasResidueTest.kt`
  - Remove correction-history residue test.
- Delete: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/TacticalArchitectureContractTest.kt`
  - Remove tactical-audit test not meant for users.
- Delete: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/HttpControllerTacticalContractTest.kt`
  - Remove tactical-audit test not meant for users.
- Delete: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/QueryHandlerTacticalContractTest.kt`
  - Remove tactical-audit residue test.
- Delete: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackIntegrationEventSubscriberContractTest.kt`
  - Remove tactical-audit residue test.
- Keep: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackContractTest.kt`
  - Preserve as the thin callback payload contract test.
- Keep: domain behavior and factory tests
  - `ContentBehaviorTest.kt`
  - `MediaProcessingTaskBehaviorTest.kt`
  - `PublicationEligibilityDomainServiceTest.kt`
  - `ContentFactoryTest.kt`
  - `MediaProcessingTaskFactoryTest.kt`
- Keep: smoke tests
  - `ContentStudioApplicationSmokeTest.kt`
  - `ContentStudioHappyPathHttpSmokeTest.kt`
- Delete at the end: `docs/superpowers/specs/2026-05-10-reference-project-teaching-simplification-design.md`
- Delete at the end: `docs/superpowers/plans/2026-05-10-reference-project-teaching-simplification.md`

## Task 1: Remove correction-history and tactical-audit tests

**Files:**
- Delete: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/BehaviorAliasResidueTest.kt`
- Delete: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/TacticalArchitectureContractTest.kt`
- Delete: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/HttpControllerTacticalContractTest.kt`
- Delete: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/QueryHandlerTacticalContractTest.kt`
- Delete: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackIntegrationEventSubscriberContractTest.kt`
- Keep unchanged: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackContractTest.kt`

- [ ] **Step 1: Delete non-teaching tests**

Remove these files:

```text
cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/BehaviorAliasResidueTest.kt
cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/TacticalArchitectureContractTest.kt
cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/HttpControllerTacticalContractTest.kt
cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/QueryHandlerTacticalContractTest.kt
cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackIntegrationEventSubscriberContractTest.kt
```

- [ ] **Step 2: Verify the remaining test set still represents the intended learning surface**

Confirm these files remain:

```text
cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehaviorTest.kt
cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/factory/ContentFactoryTest.kt
cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehaviorTest.kt
cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/factory/MediaProcessingTaskFactoryTest.kt
cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainServiceTest.kt
cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackContractTest.kt
cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioApplicationSmokeTest.kt
cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioHappyPathHttpSmokeTest.kt
```

- [ ] **Step 3: Run the full test suite after deletions**

Run:

```powershell
.\gradlew.bat test --no-daemon --rerun-tasks --no-build-cache
```

Expected:
- `BUILD SUCCESSFUL`
- no references to the deleted tactical-audit tests

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "test: remove tactical audit residue from reference project"
```

## Task 2: Reorder root Gradle configuration for learner readability

**Files:**
- Modify: `build.gradle.kts`

- [ ] **Step 1: Reorder the root build file into a user-facing sequence**

Restructure `build.gradle.kts` so the major sections appear in this order:

```kotlin
plugins { ... }

allprojects { ... }

val contentStudioSchemaPath = ...

cap4k {
    project { ... }
    sources { ... }
    generators { ... }
    layout { ... }
}

tasks.register("syncGeneratedSnapshots") { ... }

subprojects { ... }
```

Do not change behavior. Only reorder and tighten comments/spacing so a reader can scan:
- where modules are declared
- what inputs exist
- what generators are enabled
- where analysis output goes
- how snapshots are synced

- [ ] **Step 2: Verify build script still produces the same working tasks**

Run:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots cap4kAnalysisPlan cap4kAnalysisGenerate --no-daemon --rerun-tasks --no-build-cache
```

Expected:
- `BUILD SUCCESSFUL`
- analysis outputs still appear under `analysis/flows`
- snapshot sync still succeeds

- [ ] **Step 3: Commit**

```bash
git add build.gradle.kts
git commit -m "build: simplify root gradle teaching surface"
```

## Task 3: Rewrite README learning path

**Files:**
- Modify: `README.md`
- Modify: `README.zh-CN.md`

- [ ] **Step 1: Add a short “How to read this repo” section to `README.md`**

Add a concise section that explains:

```markdown
## How To Read This Repo

- Start with the domain behavior and factory tests if you want to understand business rules.
- Use the smoke tests if you want to understand the full runnable path.
- Ignore generated snapshots on first read; return to them after the handwritten flow is clear.
```

Keep the wording aligned with the existing repo structure and avoid mentioning deleted tactical cleanup tests.

- [ ] **Step 2: Add a short Gradle task map to `README.md`**

Add a concise section that explains:

```markdown
## Main Gradle Entry Points

- Run the app:
  - `./gradlew :cap4k-reference-content-studio-start:bootRun`
- Regenerate artifacts:
  - `./gradlew cap4kPlan cap4kGenerate syncGeneratedSnapshots`
- Generate analysis reports:
  - `./gradlew cap4kAnalysisPlan cap4kAnalysisGenerate`
```

Keep it short and user-facing.

- [ ] **Step 3: Mirror both sections in `README.zh-CN.md`**

Add equivalent Chinese sections with the same three-way split:
- how to read the repo
- run
- generate
- analysis

- [ ] **Step 4: Re-read both README files for misleading tactical residue language**

Remove or tighten any wording that suggests users should care about tactical cleanup or architecture-audit tests.

- [ ] **Step 5: Commit**

```bash
git add README.md README.zh-CN.md
git commit -m "docs: simplify reference project learning path"
```

## Task 4: Remove temporary working docs from the reference repository

**Files:**
- Delete: `docs/superpowers/specs/2026-05-10-reference-project-teaching-simplification-design.md`
- Delete: `docs/superpowers/plans/2026-05-10-reference-project-teaching-simplification.md`

- [ ] **Step 1: Delete the temporary spec and plan**

Remove:

```text
docs/superpowers/specs/2026-05-10-reference-project-teaching-simplification-design.md
docs/superpowers/plans/2026-05-10-reference-project-teaching-simplification.md
```

Do not delete older working docs in the same directory during this task unless they are also explicitly part of this slice.

- [ ] **Step 2: Run final full verification**

Run:

```powershell
.\gradlew.bat test --no-daemon --rerun-tasks --no-build-cache
.\gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots cap4kAnalysisPlan cap4kAnalysisGenerate --no-daemon --rerun-tasks --no-build-cache
git diff --check
```

Expected:
- both Gradle commands end with `BUILD SUCCESSFUL`
- `git diff --check` reports no whitespace errors

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: remove teaching simplification working docs"
```

## Self-Review

- Spec coverage:
  - test-surface simplification: covered by Task 1
  - Gradle teaching-surface simplification: covered by Task 2
  - README learning-path clarification: covered by Task 3
  - working-doc cleanup after implementation: covered by Task 4
- Placeholder scan:
  - no `TODO`, `TBD`, or “similar to” placeholders remain
- Type consistency:
  - tasks use exact file paths already present in the repo and do not introduce new type names
