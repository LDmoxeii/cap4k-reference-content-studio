# cap4k Reference Content Studio Tactical Realignment Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Realign `cap4k-reference-content-studio` so the delivered `v1` repository dogfoods the intended `cap4k` tactical execution path, repository/factory families, payload boundaries, and generated-skeleton contract.

**Architecture:** Keep the business scenario and module topology intact, but refactor the tactical path. `Mediator` becomes the explicit entry and orchestration gateway; built-in repository and aggregate-factory families become the real default path; generated handlers remain `SKIP`-style skeletons; HTTP contracts move to payload surfaces; specification is removed from the active `v1` evidence line.

**Tech Stack:** Kotlin, Spring Boot, Spring Data JPA, H2, `ddd-core`, `ddd-domain-repo-jpa`, `cap4k` pipeline generation, `.http` operator surfaces, JUnit 5.

---

### Task 1: Refresh generation contract and remove specification from v1

**Files:**
- Modify: `build.gradle.kts`
- Modify: `design/design.json`
- Modify: `README.md`
- Modify: `http/content.http`
- Modify: `http/review.http`
- Modify: `http/query.http`
- Test/Verify: generated outputs under `cap4k-reference-content-studio-domain/src-generated/main/kotlin`, `cap4k-reference-content-studio-application/src-generated/main/kotlin`, `cap4k-reference-content-studio-adapter/src-generated/main/kotlin`

- [ ] **Step 1: Update generator configuration for the corrected v1 evidence surface**

  Change `build.gradle.kts` so the active generator set matches the intended evidence surface:
  - enable `designApiPayload`
  - keep command/query/client/domain-event families enabled
  - keep aggregate `factory`
  - turn aggregate `specification` off

- [ ] **Step 2: Add explicit api-payload design entries if the current design input does not already produce the needed HTTP payloads**

  Update `design/design.json` so the HTTP-facing request/response shapes needed by controllers can be generated as adapter payloads instead of being handwritten request/response leakage from application contracts.

- [ ] **Step 3: Run generation and snapshot sync**

  Run:

  ```bash
  ./gradlew.bat cap4kPlan cap4kGenerate syncGeneratedSnapshots --no-daemon
  ```

  Expected:
  - payload artifacts appear under `adapter.portal.api.payload`
  - no new specification artifacts remain in the active v1 snapshot contract
  - factory artifacts exist if the framework currently supports them for this project shape

- [ ] **Step 4: Inspect generated output and align docs/operator surfaces with the corrected contract**

  Update README and `.http` files only where generation/payload changes alter the operator-visible contract. Do not rewrite the scenario.

- [ ] **Step 5: Commit**

  ```bash
  git add build.gradle.kts design/design.json README.md http/*.http cap4k-reference-content-studio-*/src-generated docs/superpowers/plans/2026-05-09-cap4k-reference-content-studio-tactical-realignment.md docs/superpowers/specs/2026-05-09-cap4k-reference-content-studio-tactical-realignment-design.md
  git commit -m "build: realign generated v1 evidence surface"
  ```

### Task 2: Move HTTP entry surfaces to Mediator and payload contracts

**Files:**
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/ContentController.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/ReviewController.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http/QueryController.kt`
- Modify/Add: generated or handwritten payload classes under `cap4k-reference-content-studio-adapter/src-generated/main/kotlin/.../adapter/portal/api/payload/**`
- Test: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioHappyPathHttpSmokeTest.kt`

- [ ] **Step 1: Replace direct handler injection in controllers**

  Refactor controllers so they no longer inject concrete handlers. Controllers should use `Mediator.cmd` / `Mediator.qry` and translate HTTP payloads to internal application requests.

- [ ] **Step 2: Remove inline handwritten request classes that should now be payload surfaces**

  Delete controller-local DTO classes when equivalent `api_payload` surfaces exist. Keep controller code focused on mapping and dispatch.

- [ ] **Step 3: Refactor query controller to stop returning application query response types directly**

  Query controller should translate from internal application query response to HTTP payload response. This is the main proof that adapter no longer leaks application contracts.

- [ ] **Step 4: Update HTTP smoke test and any contract tests**

  Keep the same operator flow, but assert against the corrected HTTP contract shape where payload changes require it.

- [ ] **Step 5: Run focused tests**

  ```bash
  ./gradlew.bat :cap4k-reference-content-studio-start:test --tests "*ContentStudioHappyPathHttpSmokeTest" --no-daemon
  ./gradlew.bat :cap4k-reference-content-studio-adapter:test --tests "*MediaProcessingCallbackContractTest" --no-daemon
  ```

- [ ] **Step 6: Commit**

  ```bash
  git add cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/http cap4k-reference-content-studio-adapter/src-generated/main/kotlin cap4k-reference-content-studio-start/src/test
  git commit -m "refactor: route HTTP entry through mediator and payloads"
  ```

### Task 3: Replace handwritten repository ports/adapters with Mediator-based repository usage

**Files:**
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateContentDraftCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/SubmitContentForReviewCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/ApproveContentReviewCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/PublishContentCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/StartMediaProcessingCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/MarkMediaProcessingSucceededCmd.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/GetContentDetailQryHandler.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/application/queries/GetCurrentProcessingStatusQryHandler.kt`
- Delete/Retire: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/ports/ContentRepository.kt`
- Delete/Retire: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/ports/MediaProcessingTaskRepository.kt`
- Delete/Retire: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/persistence/ContentPersistenceAdapter.kt`
- Delete/Retire: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/persistence/MediaProcessingTaskPersistenceAdapter.kt`
- Test: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/QueryHandlerTest.kt`

- [ ] **Step 1: Remove application repository ports from command/query handlers**

  Refactor handlers so repository access goes through `Mediator.repo` instead of handwritten application repository interfaces.

- [ ] **Step 2: Replace project-local persistence adapters in tests and runtime wiring**

  Update tests so they no longer treat handwritten persistence adapters as the tactical reference path.

- [ ] **Step 3: Keep query handlers Mediator-based as well**

  Query handlers should read through the same tactical repository path rather than injecting local repo wrappers directly.

- [ ] **Step 4: Delete or retire the handwritten repository-wrapper files once no production code depends on them**

  Do not leave dead tactical residue in the active codebase.

- [ ] **Step 5: Run focused repository/query tests**

  ```bash
  ./gradlew.bat :cap4k-reference-content-studio-adapter:test --tests "*QueryHandlerTest" --no-daemon
  ```

- [ ] **Step 6: Commit**

  ```bash
  git add cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter cap4k-reference-content-studio-adapter/src/test
  git commit -m "refactor: dogfood mediator repository path"
  ```

### Task 4: Dogfood aggregate factories and remove direct aggregate construction

**Files:**
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/content/workflow/CreateContentDraftCmd.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands/media/processing/StartMediaProcessingCmd.kt`
- Verify/Modify if generated: aggregate factory files under domain `src-generated` or live generated output
- Test: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/content/ContentBehaviorTest.kt`
- Test: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/aggregates/media_processing_task/MediaProcessingTaskBehaviorTest.kt`

- [ ] **Step 1: Identify the generated aggregate-factory surface for Content and MediaProcessingTask**

  Use the freshly generated outputs as the source of truth. If the expected factory artifacts are absent, stop and capture that as a concrete framework-usage mismatch before coding around it.

- [ ] **Step 2: Refactor command handlers to create aggregate roots through `Mediator.fac`**

  `CreateContentDraftCmd.Handler` and `StartMediaProcessingCmd.Handler` should no longer construct aggregate roots directly if factory generation is available.

- [ ] **Step 3: Keep business behavior in aggregate methods, not in the factory**

  This task is about creation path dogfood, not about moving domain logic into factories.

- [ ] **Step 4: Run domain tests**

  ```bash
  ./gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*ContentBehaviorTest" --tests "*MediaProcessingTaskBehaviorTest" --no-daemon
  ```

- [ ] **Step 5: Commit**

  ```bash
  git add cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands cap4k-reference-content-studio-domain/src-generated/main/kotlin cap4k-reference-content-studio-domain/src/test
  git commit -m "refactor: dogfood aggregate factory path"
  ```

### Task 5: Restore proper unit-of-work semantics and clean up transaction workaround shape

**Files:**
- Modify: the six write command handler files listed in Task 3
- Modify: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/WriteCommandTransactionBoundaryTest.kt`
- Modify if needed: `cap4k-reference-content-studio-start/src/test/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioApplicationSmokeTest.kt`

- [ ] **Step 1: Rework write-command handlers to use `Mediator.uow` instead of explicit transaction patching as the primary tactical signal**

  The end state should prove the intended unit-of-work path, not merely preserve green tests through `@Transactional`.

- [ ] **Step 2: Update transaction-boundary regression to assert the corrected tactical path**

  This test should stop encoding the old workaround shape as the contract.

- [ ] **Step 3: Run focused transaction test**

  ```bash
  ./gradlew.bat :cap4k-reference-content-studio-start:test --tests "*WriteCommandTransactionBoundaryTest" --no-daemon
  ```

- [ ] **Step 4: Commit**

  ```bash
  git add cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/commands cap4k-reference-content-studio-start/src/test
  git commit -m "refactor: restore mediator unit-of-work path"
  ```

### Task 6: Clean up domain-service wiring and subscriber readability

**Files:**
- Modify: `cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainService.kt`
- Modify: `cap4k-reference-content-studio-start/src/main/kotlin/com/only4/cap4k/reference/contentstudio/start/ContentStudioApplication.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/content/ContentReviewApprovedDomainEventSubscriber.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/subscribers/domain/media_processing_task/MediaProcessingSucceededDomainEventSubscriber.kt`
- Modify: `cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/integration/MediaProcessingCallbackIntegrationEventSubscriber.kt`
- Modify: `cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application/transition/MediaProcessingSucceededTransitionSurface.kt`
- Test: `cap4k-reference-content-studio-adapter/src/test/kotlin/com/only4/cap4k/reference/contentstudio/adapter/MediaProcessingCallbackIntegrationEventSubscriberTest.kt`
- Test: `cap4k-reference-content-studio-domain/src/test/kotlin/com/only4/cap4k/reference/contentstudio/domain/services/PublicationEligibilityDomainServiceTest.kt`

- [ ] **Step 1: Annotate the handwritten domain service with `@DomainService` and remove start-module `@Bean` registration**

  Keep the service handwritten, but stop using the current boot-wiring shape as the reference default.

- [ ] **Step 2: Rename subscriber and transition methods to semantic names**

  Replace generic `on(...)` naming where these files are meant to serve as reference material.

- [ ] **Step 3: Route orchestration progression through `Mediator.cmd` instead of direct `RequestSupervisor` injection**

  This includes domain subscriber and handwritten transition-surface progression.

- [ ] **Step 4: Run focused tests**

  ```bash
  ./gradlew.bat :cap4k-reference-content-studio-domain:test --tests "*PublicationEligibilityDomainServiceTest" --no-daemon
  ./gradlew.bat :cap4k-reference-content-studio-adapter:test --tests "*MediaProcessingCallbackIntegrationEventSubscriberTest" --no-daemon
  ```

- [ ] **Step 5: Commit**

  ```bash
  git add cap4k-reference-content-studio-domain/src/main/kotlin/com/only4/cap4k/reference/contentstudio/domain/services cap4k-reference-content-studio-start/src/main/kotlin/com/only4/cap4k/reference/contentstudio/start cap4k-reference-content-studio-application/src/main/kotlin/com/only4/cap4k/reference/contentstudio/application cap4k-reference-content-studio-adapter/src/main/kotlin/com/only4/cap4k/reference/contentstudio/adapter/integration cap4k-reference-content-studio-*/src/test
  git commit -m "refactor: clean domain service wiring and subscriber semantics"
  ```

### Task 7: Full regression, issue updates, and branch handoff

**Files:**
- Modify if needed: `README.md`
- Modify if needed: `.http` files
- Update issue references in docs if paths changed

- [ ] **Step 1: Run the full project verification suite**

  ```bash
  ./gradlew.bat test --no-daemon
  ./gradlew.bat cap4kPlan --no-daemon
  ./gradlew.bat cap4kGenerate --no-daemon
  ./gradlew.bat syncGeneratedSnapshots --no-daemon
  git diff --check
  git status --short
  ```

- [ ] **Step 2: Review generated snapshot diffs and remove accidental handwritten-path artifacts**

  The repo should not finish with stray generated residue under the wrong source roots.

- [ ] **Step 3: Update issue lifecycle**

  - `cap4k-reference-content-studio#1`: link spec, link plan, mark lifecycle milestones that are now true
  - `cap4k#27`: add progress comment referencing corrected v1 branch/commit once merged

- [ ] **Step 4: Commit final polish**

  ```bash
  git add README.md http/*.http docs/superpowers/specs/2026-05-09-cap4k-reference-content-studio-tactical-realignment-design.md docs/superpowers/plans/2026-05-09-cap4k-reference-content-studio-tactical-realignment.md
  git commit -m "docs: finalize tactical realignment evidence"
  ```
