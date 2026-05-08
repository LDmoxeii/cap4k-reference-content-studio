# cap4k-reference-content-studio

`cap4k-reference-content-studio` is the official runnable reference project for `cap4k`.

## Prerequisites

1. Publish local `cap4k` artifacts to `mavenLocal()`.
2. Use JDK 21.

## Current Repo State

- This repository currently contains the root Gradle skeleton for the reference project.
- The four-module layout is defined in `settings.gradle.kts` and will be filled in by later tasks.

## Shortest Runnable Path

Later tasks will add the runnable start module and the first end-to-end application flow. When those pieces land:

1. Start the application from `cap4k-reference-content-studio-start`.
2. Use the `.http` files under `http/` to walk the main happy path.

## Planned Contract Surfaces

Later tasks will add the main interaction and snapshot surfaces for this repo:

- `.http` files as the primary manual interaction surface
- runtime OpenAPI exposed by the running application
- `openapi/content-studio-openapi.json` as the committed static contract snapshot
- `src-generated/main/kotlin` roots as committed snapshot evidence, not compile-time source roots
