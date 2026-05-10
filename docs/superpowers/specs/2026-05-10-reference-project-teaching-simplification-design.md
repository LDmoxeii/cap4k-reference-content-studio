# cap4k-reference-content-studio Teaching Simplification Design

## Background

`cap4k-reference-content-studio` has reached a technically correct state for:

- runnable happy-path demonstration
- generated snapshot evidence
- analysis report generation
- tactical realignment with `Mediator`, built-in repositories, generated factories, and `api_payload`

But the repository still carries two kinds of implementation residue that are poor teaching surfaces:

1. tests that primarily document how the project was corrected rather than what the user should learn
2. Gradle/build explanations that are technically valid but still heavier than a reference project needs for first-pass learning

This slice does not change the business workflow, tactical contracts, or generator/runtime behavior. It only simplifies how the reference project presents itself to readers.

## Goal

Make the repository easier to learn from without regressing the already-verified runtime, generation, or analysis paths.

The resulting project should teach three things clearly:

1. how the domain behavior works
2. how the end-to-end HTTP happy path works
3. how the main Gradle entry points fit together for run, generate, and analysis

## Non-Goals

- do not redesign the content workflow
- do not weaken the existing happy-path smoke coverage
- do not replace the working `buildSrc` analysis convention with a new mechanism
- do not re-open tactical realignment questions already settled in earlier slices
- do not turn the repository into a Gradle showcase

## Design

### Test Surface

The test suite should be reorganized around learning value rather than correction history.

#### Keep

The following tests remain part of the reference project because they directly teach project behavior:

- domain behavior tests
  - `ContentBehaviorTest`
  - `MediaProcessingTaskBehaviorTest`
  - `PublicationEligibilityDomainServiceTest`
- domain factory tests
  - `ContentFactoryTest`
  - `MediaProcessingTaskFactoryTest`
- integration/full-path verification
  - `ContentStudioApplicationSmokeTest`
  - `ContentStudioHappyPathHttpSmokeTest`

`MediaProcessingCallbackContractTest` remains because it proves the callback integration-event payload contract itself rather than cleanup residue.

#### Remove

The following tests should be removed because they are correction-history or tactical-audit artifacts rather than user-facing reference-project learning surfaces:

- `BehaviorAliasResidueTest`
- `TacticalArchitectureContractTest`
- `HttpControllerTacticalContractTest`

The following tests should also be removed because they only prove tactical cleanup rather than a stable project contract:

- `QueryHandlerTacticalContractTest`
- `MediaProcessingCallbackIntegrationEventSubscriberContractTest`

The removal criterion is simple:

- keep tests that explain business behavior or the runnable workflow
- remove tests that mainly assert that some previous refactor residue no longer exists

### Gradle Surface

The build should become easier to read, but only through safe simplification.

#### Keep

- `buildSrc` convention plugin for Kotlin and analysis compiler wiring
- root `cap4k` DSL configuration
- existing `syncGeneratedSnapshots`
- existing analysis tasks:
  - `cap4kAnalysisPlan`
  - `cap4kAnalysisGenerate`

These are already proven to work and should not be replaced in this slice.

#### Simplify

- reorder root `build.gradle.kts` so the file reads in a user-facing sequence:
  1. plugins and repositories
  2. project/module mapping
  3. source inputs
  4. generators
  5. analysis/report outputs
  6. snapshot sync tasks
- reduce avoidable explanatory noise in module `build.gradle.kts`
- keep configuration explicit where it helps learning, even if that means a small amount of duplication remains

The target is not minimum line count. The target is that a reader can understand:

- how to run the app
- how to regenerate artifacts
- how to generate analysis reports

without reverse-engineering the build.

### README Surface

Both `README.md` and `README.zh-CN.md` should be updated to explain the repository in three entry paths:

1. run the app
2. regenerate artifacts
3. generate analysis reports

They should also briefly explain the test structure:

- domain tests for rules and factories
- smoke tests for runtime and full happy path

They should stop implying that users need to understand tactical cleanup tests in order to learn the project.

## Acceptance Criteria

- the repository no longer contains correction-history tests such as `BehaviorAliasResidueTest`
- the repository no longer contains tactical-audit tests such as `TacticalArchitectureContractTest`, `HttpControllerTacticalContractTest`, `QueryHandlerTacticalContractTest`, and `MediaProcessingCallbackIntegrationEventSubscriberContractTest`
- any remaining adapter/start tests clearly serve runtime or contract verification rather than cleanup archaeology
- root Gradle configuration reads in a simpler, user-oriented order without changing the verified build mechanism
- README files clearly separate run, generate, and analysis entry points
- full verification still passes:
  - `./gradlew.bat test --no-daemon --rerun-tasks --no-build-cache`
  - `./gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots cap4kAnalysisPlan cap4kAnalysisGenerate --no-daemon --rerun-tasks --no-build-cache`

## Temporary Working-Docs Rule

`docs/superpowers/specs/**` and `docs/superpowers/plans/**` in this repository are implementation-working artifacts only.

This slice may use them for spec/plan execution, but once implementation is complete and the repository state is stable, these working docs should be deleted from the reference repository.
