# Remove Generated Snapshot Surface Design

Date: 2026-05-18

## Purpose

Remove the `cap4kGenerateSources` plus `src-generated` snapshot workflow from `cap4k-reference-content-studio`.

The current reference project mixes two different ideas:

- design and schema are the modeling and generation inputs;
- committed `src-generated` trees are presented as a generated evidence surface.

That second idea is misleading. It suggests that committed generated snapshots are part of the authoring contract, even though the actual design-family surfaces already live as checked-in sources under `src/main`, and the real ownership evidence comes from fresh `cap4kPlan` output.

This change should make the project teach the stricter boundary:

- authoring truth lives in generation inputs such as `design/design.json` and schema;
- ownership truth lives in fresh `build/cap4k/plan.json`;
- implementation truth lives in the checked-in handwritten or generator-owned files that the current plan targets;
- committed `src-generated` snapshots are not needed as a separate educational layer.

## Decision

Delete the `src-generated` snapshot workflow from this repository entirely.

After this change:

- the root build no longer exposes `syncGeneratedSnapshots`;
- the root build no longer depends on `cap4kGenerateSources` as a committed snapshot export path;
- committed `src-generated/main/kotlin` trees are removed from the domain and adapter modules;
- README defaults to Chinese, with `README.md` as the primary document and `README.en.md` as the English copy;
- `README.zh-CN.md` is removed;
- documents in this repository no longer tell readers to inspect or refresh `src-generated` as part of normal understanding or verification.

## Current State

The current repository still carries a snapshot-oriented workflow:

- `build.gradle.kts` registers root and per-subproject `syncGeneratedSnapshots` tasks;
- those tasks copy `build/generated/cap4k/main/kotlin` into committed `src-generated/main/kotlin`;
- `README.md` and `README.zh-CN.md` both describe `src-generated` as a committed snapshot surface and tell readers to return to it after understanding handwritten flow;
- the paid-publication implementation plan still uses `src-generated` directories as task scope, diff scope, and evidence scope.

Fresh baseline evidence from this repository also shows that the snapshot path is still active in practice:

- `.\gradlew.bat test cap4kPlan` succeeds;
- that same baseline command triggers `:cap4kGenerateSources` before normal module compilation;
- the compile path already works from generated build output and checked-in source surfaces, which means `src-generated` is acting as an extra committed evidence layer rather than the primary business authoring surface.

## Goals

1. Remove the misleading idea that committed generated snapshots are part of the normal cap4k business authoring contract in this reference project.
2. Keep the project buildable and reviewable without `src-generated`.
3. Make the Chinese README the primary entry point.
4. Keep historical planning docs from teaching the removed workflow as if it were still current practice.

## Non-Goals

- Do not change `cap4k` main-repository generator behavior in this task.
- Do not redesign `cap4kPlan` or `cap4kGenerate` task semantics across repos.
- Do not re-author the project's business behavior, command logic, query logic, or service-integration logic.
- Do not introduce a renamed replacement for committed generated snapshots.

## Proposed Changes

## Build Changes

Update `build.gradle.kts` to remove the snapshot export workflow.

Required changes:

- delete the root `syncGeneratedSnapshots` task;
- delete the `subprojects { ... syncGeneratedSnapshots ... }` block;
- remove task descriptions and examples that position `cap4kGenerateSources` as part of the repository's committed evidence surface.

This change is intentionally repo-local. It does not claim that `cap4kGenerateSources` must disappear from `cap4k` itself in the same PR. The task only removes this reference project's explicit teaching and committed usage of that path.

## Repository Content Changes

Delete these committed trees:

- `cap4k-reference-content-studio-domain/src-generated/main/kotlin/**`
- `cap4k-reference-content-studio-adapter/src-generated/main/kotlin/**`

Do not replace them with another committed generated snapshot directory.

The remaining reviewable surfaces should be:

- input sources such as `design/design.json` and schema;
- actual checked-in command/query/client/payload/subscriber files targeted by fresh plan output;
- generated build output under `build/` when local verification is needed;
- fresh `build/cap4k/plan.json` and analysis output.

## README Strategy

Switch README priority to Chinese-first:

- promote the current Chinese document into `README.md`;
- keep the current English document as `README.en.md`;
- delete `README.zh-CN.md`.

Both README variants must be updated to remove `src-generated`, `syncGeneratedSnapshots`, and `cap4kGenerateSources` as user-facing workflow concepts.

The new README contract should emphasize:

- model and regenerate from inputs;
- inspect `build/cap4k/plan.json` when ownership questions matter;
- verify behavior through compile, tests, and analysis output;
- do not treat committed generated snapshots as a required reading layer.

## Historical Doc Cleanup

Update repository-local historical docs that still instruct readers to use the removed snapshot path as if it remains current workflow.

The minimum required cleanup target is:

- `docs/superpowers/plans/2026-05-17-paid-publication-saga.md`

That plan currently:

- scopes tasks around `src-generated`;
- uses `syncGeneratedSnapshots` in command examples;
- stages `src-generated` directories for commit;
- treats `src-generated` diffs as expected verification evidence.

Those references should be rewritten to current truth, not merely left as historical noise, because they are still likely to be read as implementation guidance.

If other repository docs still teach the removed workflow, they should be updated in the same pass.

## Verification

Use focused repo-local verification only.

Required checks:

1. `.\gradlew.bat cap4kPlan`
2. `.\gradlew.bat test`
3. `git diff --check`
4. `rg -n "src-generated|syncGeneratedSnapshots|cap4kGenerateSources" README* docs/superpowers build.gradle.kts`

Expected outcomes:

- the build no longer defines or documents the snapshot-sync task;
- the repository no longer contains committed `src-generated` trees;
- README primary entry is `README.md` in Chinese and `README.en.md` remains available;
- the project still plans and tests successfully without committed snapshots.

## Risks

## Hidden Compile Dependency

The main risk is that some module may still compile only because committed `src-generated` files are present.

Current evidence suggests that risk is low, because:

- the repo-local Gradle setup explicitly copies build-generated Kotlin into `src-generated`;
- no additional source-set configuration has been found that makes `src-generated` a required custom source root;
- fresh baseline build already compiles after generating into `build/generated/cap4k/main/kotlin`.

Still, this must be proven by verification after deletion, not assumed.

## History Drift

Deleting the files but leaving old plans and README language unchanged would create a new kind of drift: docs would continue describing a workflow that no longer exists.

That is why doc cleanup is part of the required scope, not optional polish.

## Success Criteria

- No committed `src-generated` directories remain.
- No `syncGeneratedSnapshots` task remains in the root build.
- `README.md` is Chinese-first, `README.en.md` remains, and `README.zh-CN.md` is removed.
- Repository docs no longer teach committed generated snapshots as part of the active workflow.
- `cap4kPlan` and tests still pass after the cleanup.
