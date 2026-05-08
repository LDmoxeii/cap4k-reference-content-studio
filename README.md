# cap4k-reference-content-studio

`cap4k-reference-content-studio` is the official runnable reference project for `cap4k`.

## Prerequisites

1. Publish local `cap4k` artifacts to `mavenLocal()`.
2. Use JDK 21.

## Shortest Path

1. Start the application from `cap4k-reference-content-studio-start`.
2. Use the `.http` files under `http/` to walk the main happy path.

## Contract Surfaces

- `.http` files are the primary manual interaction surface.
- Runtime OpenAPI is exposed by the running application.
- `openapi/content-studio-openapi.json` is the committed static contract snapshot.
- `src-generated/main/kotlin` roots are committed snapshot evidence, not compile-time source roots.
