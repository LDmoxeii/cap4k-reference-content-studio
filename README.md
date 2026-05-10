# cap4k-reference-content-studio

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
- Ignore generated snapshots on first read; return to `src-generated` after the handwritten flow is clear.

## What Is In This Repo

The repository is organized as four Gradle modules:

- `cap4k-reference-content-studio-domain`: domain model plus committed generated domain snapshots
- `cap4k-reference-content-studio-application`: application commands and queries
- `cap4k-reference-content-studio-adapter`: HTTP controllers, query adapters, persistence adapters
- `cap4k-reference-content-studio-start`: the Spring Boot runtime you actually start locally

For local operation, treat this as a small reference application with a single
Spring Boot process and an in-memory H2 database.

## Prerequisites

1. Use JDK 17.
2. Publish the required `cap4k` snapshot artifacts to `mavenLocal()` before
   you build or start this repo.

From your local `cap4k` checkout, run the publish step first:

```bash
./gradlew publishToMavenLocal
```

This repo depends on `0.5.0-SNAPSHOT` artifacts from `mavenLocal()`. If you
skip that step, Gradle resolution will fail even if this repository itself is
checked out correctly.

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
- Regenerate artifacts:
  - `./gradlew cap4kPlan cap4kGenerate syncGeneratedSnapshots`
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

The remaining committed tests are there to support the runnable reference workflow or a thin callback contract. They are not meant to replace the domain-first reading path.

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

## What `src-generated` Means

`src-generated/main/kotlin` directories are committed generated snapshots. They
exist as reference evidence for what `cap4k` generated for this example.

They are not the primary handwritten source roots, and they are not where you
should start when trying to understand the runtime flow. The handwritten code
lives under the normal `src/main/kotlin` directories. When generator output is
refreshed, `syncGeneratedSnapshots` copies fresh artifacts into `src-generated`
for review and snapshot tracking.

## Version One Scope

Version one is intentionally narrow. It covers the local reference workflow only:

- one local Spring Boot process
- one in-memory H2 runtime
- manual operation through `.http` files
- callback simulation through the HTTP integration endpoint
- committed generation and OpenAPI snapshots for inspection

Version one does not try to cover:

- a frontend or operator console
- authentication or authorization
- a real external media-processing system
- production deployment, observability, or scaling guidance
- a broader editorial workflow beyond this single happy path
