# Remove Generated Snapshot Surface Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the `cap4kGenerateSources` plus `src-generated` snapshot workflow from `cap4k-reference-content-studio`, delete committed snapshot trees, and switch the repository to Chinese-first README entry.

**Architecture:** This is a repo-local cleanup. The build stops exporting committed generated snapshots, the repository stops tracking `src-generated` trees, and docs stop teaching snapshot directories as part of the active authoring contract. Ownership explanation moves to generation inputs plus fresh `build/cap4k/plan.json`, while README priority flips to Chinese-first by promoting the former `README.zh-CN.md` into `README.md`.

**Tech Stack:** Gradle Kotlin DSL, Kotlin/Spring Boot project structure, Markdown, Git, PowerShell, ripgrep, Gradle wrapper.

---

## File Structure

Build and repository wiring:

- Modify: `build.gradle.kts`
- Delete: `cap4k-reference-content-studio-domain/src-generated/main/kotlin/**`
- Delete: `cap4k-reference-content-studio-adapter/src-generated/main/kotlin/**`

README and repo entrypoints:

- Rename: `README.md` -> `README.en.md`
- Rename: `README.zh-CN.md` -> `README.md`
- Modify: new `README.md`
- Modify: `README.en.md`
- Delete: original `README.zh-CN.md`

Historical guidance cleanup:

- Modify: `docs/superpowers/plans/2026-05-17-paid-publication-saga.md`

Verification outputs to inspect after the change:

- Inspect: `build/cap4k/plan.json`
- Inspect: `build/reports/problems/problems-report.html` only if Gradle reports a new failure

## Task 1: Remove Snapshot Export Tasks From The Build

**Files:**

- Modify: `build.gradle.kts`

- [ ] **Step 1: Capture the current snapshot-task footprint**

Run:

```powershell
rg -n "import org.gradle.api.tasks.Sync|syncGeneratedSnapshots|cap4kGenerateSources|src-generated" build.gradle.kts
```

Expected:

- matches for the `Sync` import;
- matches for the root `syncGeneratedSnapshots` task;
- matches for the subproject `syncGeneratedSnapshots` task block;
- matches for the `into(layout.projectDirectory.dir("src-generated/main/kotlin"))` line.

- [ ] **Step 2: Remove the snapshot export workflow from `build.gradle.kts`**

Make these exact edits:

1. Remove the unused import:

```kotlin
import org.gradle.api.tasks.Sync
```

2. Delete the entire root task block:

```kotlin
tasks.register("syncGeneratedSnapshots") {
    group = "verification"
    description = "Sync generated artifact snapshots into src-generated roots."
    dependsOn(tasks.named("cap4kGenerateSources"))
    dependsOn(
        subprojects.map { project ->
            project.tasks.named("syncGeneratedSnapshots")
        }
    )
}
```

3. Delete the entire subproject sync block at the end of the file:

```kotlin
subprojects {
    val generatedKotlinSourcesDir = layout.buildDirectory.dir("generated/cap4k/main/kotlin")
    tasks.register<Sync>("syncGeneratedSnapshots") {
        group = "verification"
        description = "Sync generated artifact snapshots into src-generated/main/kotlin."
        dependsOn(rootProject.tasks.named("cap4kGenerateSources"))
        from(generatedKotlinSourcesDir)
        into(layout.projectDirectory.dir("src-generated/main/kotlin"))
        includeEmptyDirs = false
    }
}
```

After the edit, the file should end with:

```kotlin
tasks.named("cap4kAnalysisGenerate") {
    finalizedBy(tasks.named("normalizeAnalysisFlowIndex"))
}
```

- [ ] **Step 3: Verify the build no longer defines `syncGeneratedSnapshots`**

Run:

```powershell
.\gradlew.bat help --task syncGeneratedSnapshots
```

Expected:

- command exits non-zero;
- output contains `Task 'syncGeneratedSnapshots' not found`.

- [ ] **Step 4: Verify the build still plans successfully**

Run:

```powershell
.\gradlew.bat cap4kPlan
```

Expected:

- `BUILD SUCCESSFUL`
- fresh `build/cap4k/plan.json` still exists.

- [ ] **Step 5: Commit the build cleanup**

Run:

```powershell
git add build.gradle.kts
git commit -m "build: remove generated snapshot sync tasks"
```

Expected:

- one commit containing only the build-task removal.

## Task 2: Delete The Committed `src-generated` Trees

**Files:**

- Delete: `cap4k-reference-content-studio-domain/src-generated/main/kotlin/**`
- Delete: `cap4k-reference-content-studio-adapter/src-generated/main/kotlin/**`

- [ ] **Step 1: Prove the committed snapshot trees currently exist**

Run:

```powershell
rg --files cap4k-reference-content-studio-domain/src-generated cap4k-reference-content-studio-adapter/src-generated
```

Expected:

- Kotlin files under `cap4k-reference-content-studio-domain/src-generated/main/kotlin/**`
- Kotlin files under `cap4k-reference-content-studio-adapter/src-generated/main/kotlin/**`

- [ ] **Step 2: Delete the committed snapshot trees**

Run:

```powershell
git rm -r cap4k-reference-content-studio-domain/src-generated
git rm -r cap4k-reference-content-studio-adapter/src-generated
```

Expected:

- Git stages removal of both trees, including generated aggregate/meta files and generated repository files.

- [ ] **Step 3: Verify the directories are gone**

Run:

```powershell
rg --files cap4k-reference-content-studio-domain/src-generated cap4k-reference-content-studio-adapter/src-generated
```

Expected:

- no output;
- command exits non-zero because the directories no longer exist.

- [ ] **Step 4: Run the compile-and-test baseline without committed snapshots**

Run:

```powershell
.\gradlew.bat test cap4kPlan
```

Expected:

- `BUILD SUCCESSFUL`
- no compiler failure caused by removing the committed snapshot trees.

- [ ] **Step 5: Commit the snapshot-directory deletion**

Run:

```powershell
git add -u cap4k-reference-content-studio-domain/src-generated cap4k-reference-content-studio-adapter/src-generated
git commit -m "refactor: drop committed generated snapshots"
```

Expected:

- one commit containing only the staged snapshot-directory deletions.

## Task 3: Switch To Chinese-First README And Remove Snapshot Language

**Files:**

- Rename: `README.md` -> `README.en.md`
- Rename: `README.zh-CN.md` -> `README.md`
- Modify: `README.md`
- Modify: `README.en.md`

- [ ] **Step 1: Rename the README files into the new priority order**

Run:

```powershell
git mv README.md README.en.md
git mv README.zh-CN.md README.md
```

Expected:

- `README.md` now contains the former Chinese content;
- `README.en.md` now contains the former English content.

- [ ] **Step 2: Rewrite the Chinese README to remove snapshot workflow guidance**

Edit `README.md` with these exact content changes.

Replace the third bullet under `## 如何阅读这个仓库`:

```markdown
- 第一次阅读时先看 `design/design.json`、数据库 schema、`build/cap4k/plan.json`，再对照 `src/main/kotlin` 下的真实实现文件；不要把 `src-generated` 当成阅读入口。
```

Replace the four module bullets under `## 仓库内容` with:

```markdown
- `cap4k-reference-content-studio-domain`：领域模型、factory、domain service 和领域行为测试
- `cap4k-reference-content-studio-application`：应用层命令、查询、订阅器、Saga 和 job
- `cap4k-reference-content-studio-adapter`：HTTP 控制器、查询适配、持久化适配、外部客户端适配
- `cap4k-reference-content-studio-start`：你本地实际启动的 Spring Boot 运行时
```

Replace the regenerate command under `## 主要 Gradle 入口` with:

```markdown
- 重新生成与检查 ownership：
  - `./gradlew cap4kPlan cap4kGenerate`
```

Replace the entire `## \`src-generated\` 的含义` section with:

```markdown
## 如何检查生成 ownership

这个仓库不再提交 `src-generated` 快照目录。

如果你要检查 generator 是否严格按照输入合同和 ownership 输出，请优先看：

- `design/design.json`
- `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
- `build/cap4k/plan.json`

其中：

- `design/design.json` 和 schema 决定 generator 应该消费什么事实；
- `build/cap4k/plan.json` 决定当前 generator 计划写哪些文件、使用哪个 generator、采用什么冲突策略；
- 真正提交进仓的 command/query/client/payload/subscriber surface 仍然在各模块自己的 `src/main/kotlin` 下。
```

Replace the final bullet in `## v1 范围` with:

```markdown
- 保留 OpenAPI 快照和分析产物供检查
```

- [ ] **Step 3: Rewrite the English README to match the same contract**

Edit `README.en.md` with these exact content changes.

Replace the third bullet under `## How To Read This Repo`:

```markdown
- Start with `design/design.json`, the database schema, and fresh `build/cap4k/plan.json`, then read the real implementation under `src/main/kotlin`; do not treat `src-generated` as the reading entry point.
```

Replace the four module bullets under `## What Is In This Repo` with:

```markdown
- `cap4k-reference-content-studio-domain`: domain model, factories, domain services, and domain behavior tests
- `cap4k-reference-content-studio-application`: application commands, queries, subscribers, Saga, and jobs
- `cap4k-reference-content-studio-adapter`: HTTP controllers, query adapters, persistence adapters, and external-client adapters
- `cap4k-reference-content-studio-start`: the Spring Boot runtime you actually start locally
```

Replace the regenerate command under `## Main Gradle Entry Points` with:

```markdown
- Regenerate and inspect ownership:
  - `./gradlew cap4kPlan cap4kGenerate`
```

Replace the entire `## What \`src-generated\` Means` section with:

```markdown
## How To Inspect Generation Ownership

This repository no longer commits a `src-generated` snapshot tree.

If you need to inspect whether generator-owned surfaces still match the declared inputs, read these in order:

- `design/design.json`
- `cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql`
- `build/cap4k/plan.json`

In this repository:

- `design/design.json` and schema describe what generation should consume;
- `build/cap4k/plan.json` shows which files the current generator run plans to write, which generator owns them, and which conflict policy applies;
- the checked-in command/query/client/payload/subscriber surfaces still live under each module's `src/main/kotlin`.
```

Replace the final bullet in `## Version One Scope` with:

```markdown
- committed OpenAPI snapshots and analysis outputs for inspection
```

- [ ] **Step 4: Verify the README switch and snapshot-language removal**

Run:

```powershell
Test-Path README.zh-CN.md
rg -n "src-generated|syncGeneratedSnapshots|cap4kGenerateSources" README.md README.en.md
```

Expected:

- `Test-Path README.zh-CN.md` prints `False`;
- `rg` returns no matches.

- [ ] **Step 5: Commit the README switch**

Run:

```powershell
git add README.md README.en.md README.zh-CN.md
git commit -m "docs: make README Chinese-first"
```

Expected:

- one commit containing the README rename plus content rewrite.

## Task 4: Rewrite Historical Plan References And Run Final Verification

**Files:**

- Modify: `docs/superpowers/plans/2026-05-17-paid-publication-saga.md`

- [ ] **Step 1: Remove `src-generated` from the historical file-structure section**

In `docs/superpowers/plans/2026-05-17-paid-publication-saga.md`, replace:

```markdown
Generated snapshot roots after `cap4kGenerate` and `syncGeneratedSnapshots`:

- Modify: `cap4k-reference-content-studio-domain/src-generated/main/kotlin/**`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/**` for generated command/query/client shells only when generation produces new shells
- Modify: `cap4k-reference-content-studio-adapter/src-generated/main/kotlin/**`
```

with:

```markdown
Generation ownership evidence after `cap4kPlan` and `cap4kGenerate`:

- Inspect: `build/cap4k/plan.json`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/**` for generator-owned design surfaces when generation updates checked-in files
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/**` only for handwritten behavior/factory/domain-service code
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/**` only for handwritten handlers/controllers/adapters
```

- [ ] **Step 2: Replace command examples that still rely on snapshot sync**

Make these exact replacements in the same plan file.

Replace:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots
```

with:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate
```

Replace:

```powershell
rg -n "PaidPublicationTask|paid_publication_task|CreatePaidContentDraft|TryStartPaidPublication|ReserveCreatorPayoutHold|CreateAccessEntitlementPlan" design cap4k-reference-content-studio-domain/src-generated cap4k-reference-content-studio-adapter/src-generated
```

with:

```powershell
rg -n "PaidPublicationTask|paid_publication_task|CreatePaidContentDraft|TryStartPaidPublication|ReserveCreatorPayoutHold|CreateAccessEntitlementPlan" design cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql build/cap4k/plan.json cap4k-reference-content-studio-application/src/main/kotlin
```

Replace:

```powershell
git add design/design.json build.gradle.kts cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql cap4k-reference-content-studio-*/src-generated
git commit -m "feat: generate paid publication surfaces"
```

with:

```powershell
git add design/design.json build.gradle.kts cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql cap4k-reference-content-studio-application/src/main/kotlin cap4k-reference-content-studio-domain/src/main/kotlin cap4k-reference-content-studio-adapter/src/main/kotlin
git commit -m "feat: generate paid publication surfaces"
```

Keep `build/cap4k/plan.json` as inspection-only evidence; do not stage it because `build/` stays ignored.

Replace:

```powershell
rg --files cap4k-reference-content-studio-domain/src-generated cap4k-reference-content-studio-adapter/src-generated | rg "publication_release_readiness|PublicationReleaseReadiness"
```

with:

```powershell
rg -n "publication_release_readiness|PublicationReleaseReadiness" design cap4k-reference-content-studio-start/src/main/resources/db/schema/content-studio-schema.sql cap4k-reference-content-studio-domain/src/main/kotlin cap4k-reference-content-studio-application/src/main/kotlin cap4k-reference-content-studio-adapter/src/main/kotlin http README.md README.en.md
```

Replace:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots
git diff --exit-code -- cap4k-reference-content-studio-domain/src-generated cap4k-reference-content-studio-application/src-generated cap4k-reference-content-studio-adapter/src-generated
```

with:

```powershell
.\gradlew.bat cap4kPlan cap4kGenerate
Test-Path build/cap4k/plan.json
git diff --exit-code -- cap4k-reference-content-studio-application/src/main/kotlin cap4k-reference-content-studio-domain/src/main/kotlin cap4k-reference-content-studio-adapter/src/main/kotlin
```

- [ ] **Step 3: Verify the historical plan no longer teaches the removed workflow**

Run:

```powershell
rg -n "src-generated|syncGeneratedSnapshots|cap4kGenerateSources" docs/superpowers/plans/2026-05-17-paid-publication-saga.md
```

Expected:

- no matches.

- [ ] **Step 4: Run final repo verification**

Run:

```powershell
.\gradlew.bat cap4kPlan
.\gradlew.bat test
git diff --check
rg -n "src-generated|syncGeneratedSnapshots|cap4kGenerateSources" README.md README.en.md docs/superpowers/plans/2026-05-17-paid-publication-saga.md build.gradle.kts
```

Expected:

- both Gradle commands end with `BUILD SUCCESSFUL`;
- `git diff --check` prints nothing;
- the final `rg` command returns no matches.

- [ ] **Step 5: Commit the doc cleanup and verification**

Run:

```powershell
git add docs/superpowers/plans/2026-05-17-paid-publication-saga.md
git commit -m "docs: remove generated snapshot workflow references"
```

Expected:

- one commit containing the historical-plan cleanup;
- working tree is clean except for any optional local build artifacts ignored by Git.

## Plan Self-Review

Spec coverage check:

- build task removal is covered by Task 1;
- committed snapshot directory deletion is covered by Task 2;
- Chinese-first README switch and `README.zh-CN.md` deletion are covered by Task 3;
- historical plan cleanup is covered by Task 4;
- repo-local verification is covered by Task 4 step 4.

Placeholder scan:

- no `TODO`, `TBD`, or “implement later” placeholders remain;
- every command step has an exact command and an expected result;
- every text-edit step includes exact replacement content.

Type and naming consistency:

- the plan consistently uses `syncGeneratedSnapshots`, `cap4kGenerateSources`, `src-generated`, `README.en.md`, and the `README.zh-CN.md` -> `README.md` rename/delete step;
- no later task refers to a file or task name that earlier tasks do not define.
