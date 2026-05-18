# cap4k-reference-content-studio

[![CI](https://github.com/LDmoxeii/cap4k-reference-content-studio/actions/workflows/ci.yml/badge.svg)](https://github.com/LDmoxeii/cap4k-reference-content-studio/actions/workflows/ci.yml)
[![GitHub Release](https://img.shields.io/github/v/release/LDmoxeii/cap4k-reference-content-studio)](https://github.com/LDmoxeii/cap4k-reference-content-studio/releases)
[![GitHub license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/LDmoxeii/cap4k-reference-content-studio/blob/master/LICENSE)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/LDmoxeii/cap4k-reference-content-studio)

`cap4k-reference-content-studio` is the runnable reference project for `cap4k`.
It demonstrates a small end-to-end content workflow on top of the generated
domain/application/adapter split:

- create a content draft
- submit and approve review
- trigger media-processing completion through the HTTP integration callback
- observe the content become published

You should be able to clone this repository and run the local workflow without
reading the full `cap4k` repository first.

## How To Read This Repo

- Start with the domain behavior and factory tests if you want to understand the business rules.
- Use the smoke tests if you want to understand the runnable end-to-end path.
- Start with `design/design.json`, the database schema, and fresh `build/cap4k/plan.json`, then read the real implementation under `src/main/kotlin`; do not treat `src-generated` as the reading entry point.

## What Is In This Repo

The repository is organized as four Gradle modules:

- `cap4k-reference-content-studio-domain`: domain model, factories, domain services, and domain behavior tests
- `cap4k-reference-content-studio-application`: application commands, queries, subscribers, Saga, and jobs
- `cap4k-reference-content-studio-adapter`: HTTP controllers, query adapters, persistence adapters, and external-client adapters
- `cap4k-reference-content-studio-start`: the Spring Boot runtime you actually start locally

For local operation, treat this as a small reference application with a single
Spring Boot process and an in-memory H2 database.

## Prerequisites

1. Use JDK 17.
2. Clone the repository and run Gradle directly. This repo consumes `cap4k`
   `0.6.0` from Maven Central, so no separate `cap4k` checkout or
   local artifact bootstrap step is required.
3. The public Gradle plugin id used for cap4k pipeline tasks is
   `io.github.ldmoxeii.cap4k.pipeline`.

## Shortest Startup Path

From the repository root:

```bash
./gradlew :cap4k-reference-content-studio-start:bootRun
```

On Windows:

```powershell
.\gradlew.bat :cap4k-reference-content-studio-start:bootRun
```

The app starts on `http://localhost:8080`.

## Main Gradle Entry Points

- Run the app:
  - `./gradlew :cap4k-reference-content-studio-start:bootRun`
- Regenerate and inspect ownership:
  - `./gradlew cap4kPlan cap4kGenerate`
- Generate analysis reports:
  - `./gradlew cap4kAnalysisPlan cap4kAnalysisGenerate`

## Primary Operating Surface

The main operator surface in version one is the committed `.http` files under
`http/`. Use those to drive the workflow against a running local app.

Swagger UI may be available through Springdoc defaults, but it is not the main
way this reference project is meant to be operated. Use the `.http` files and
the actual HTTP responses first. OpenAPI is kept as a limited contract
snapshot, not as the authoritative operator guide for the whole happy path.

## `.http` Execution Order

Run the files in this order:

1. `http/content.http`
   Create the draft and copy `response.contentId`.
2. `http/review.http`
   Paste `contentId`, then run both requests in order: submit for review, then
   approve the review.
3. `http/query.http`
   Paste `contentId`, then call `GET /media-processing/{contentId}` until a
   `task` object is present. Copy `task.externalTaskId`.
4. `http/media-processing.http`
   Paste `contentId` and `externalTaskId`, then send the callback event that
   marks media processing as succeeded.
5. `http/query.http` again
   Re-run both queries until the content shows `contentStatus = PUBLISHED` and
   the task shows `processingStatus = SUCCEEDED`.

That sequence matches the local happy-path smoke coverage in this repository:
review approval creates the media-processing task, and the publish transition
happens only after the callback completes.

For the opt-in paid publication path, run `http/paid-publication.http` to create
the paid draft after you understand the default sequence.

## Advanced Authoring Examples

The default path still uses immediate publication: after media processing
succeeds, the application publishes the content through `PublishContentCmd`.

The advanced path is opt-in paid publication. It demonstrates cap4k Saga as a
compensation-oriented process, not as a waiting mechanism. The Saga reserves a
creator payout hold, creates an access entitlement plan, publishes content, and
activates the entitlement plan. If a later step fails, idempotent compensation
commands release or cancel earlier side effects where business rules allow it.

- `MediaProcessingResultSnapshot` is a handwritten JSON-backed value concept persisted through `media_processing_task.result_snapshot`.
- `PublicationEligibilityDomainService` returns an auditable publication decision.
- Paid content uses `PaidPublicationTask` to record cross-step publication state.
  `PaidPublicationSaga` coordinates payout hold reservation, entitlement plan creation,
  content publication, entitlement activation, and compensation on failure.
- `codegen/templates/design/api_payload.kt.peb` demonstrates a project-level template override:
  generated API payloads keep stable OpenAPI schema names without hand-editing generated files.

The paid path is a real Saga example, but it is not the default publication path.
The default path still publishes directly after media processing succeeds. Explicit
paid content starts paid publication after media processing succeeds, then the Saga
drives the paid publication sub-steps and records compensation when a downstream
paid-publication step fails.

`MediaProcessingResultSnapshot` is still a handwritten result snapshot. Do not
read it as complete generator support for value objects. First-class
value-object generation remains cap4k follow-up work.

## OpenAPI Location

There are two OpenAPI surfaces:

- runtime docs from the running app: `http://localhost:8080/v3/api-docs`
- committed static snapshot: `openapi/content-studio-openapi.json`

Use the runtime endpoint and committed JSON file to inspect the currently
generated documented HTTP contract. In version one, these OpenAPI surfaces do
not fully describe the callback consume path or the full query response payload
shape used by the happy-path workflow, so treat the `.http` files and live
responses as the operator truth.

## Test Surface

If you want to learn the project through tests, use this order:

- domain and factory tests:
  - rules, state transitions, aggregate creation
- smoke tests:
  - runtime boot
  - full HTTP happy path

The remaining committed tests are there to support the runnable reference workflow.
They are not meant to replace the domain-first reading path.

## Analysis Reports

This repository also enables IR-based analysis generation for the handwritten
and generated runtime flow.

Useful commands:

```bash
./gradlew cap4kAnalysisPlan
./gradlew cap4kAnalysisGenerate
```

On Windows:

```powershell
.\gradlew.bat cap4kAnalysisPlan
.\gradlew.bat cap4kAnalysisGenerate
```

What you should expect:

- module-local IR snapshots under:
  - `cap4k-reference-content-studio-domain/build/cap4k-code-analysis`
  - `cap4k-reference-content-studio-application/build/cap4k-code-analysis`
  - `cap4k-reference-content-studio-adapter/build/cap4k-code-analysis`
- root analysis plan:
  - `build/cap4k/analysis-plan.json`
- committed flow analysis outputs:
  - `analysis/flows/*.json`
  - `analysis/flows/*.mmd`

These artifacts are not part of the main happy-path operator workflow. They are
kept as a reference surface for inspecting controller, subscriber, job, and
application flow structure.

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

## Version One Scope

Version one is intentionally narrow. It covers the local reference workflow only:

- one local Spring Boot process
- one in-memory H2 runtime
- manual operation through `.http` files
- callback simulation through the HTTP integration endpoint
- committed OpenAPI snapshots and analysis outputs for inspection

Version one does not try to cover:

- a frontend or operator console
- authentication or authorization
- a real external media-processing system
- production deployment, observability, or scaling guidance
- a broader editorial workflow beyond this single happy path
