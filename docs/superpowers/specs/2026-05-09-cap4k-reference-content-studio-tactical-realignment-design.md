# cap4k Reference Content Studio Tactical Realignment Design

## Background

`cap4k-reference-content-studio` was delivered as the first runnable reference project for the `cap4k#27` line.

It already proves several important things:

- the project can be bootstrapped into a runnable multi-module shape
- the content publishing and media-processing scenario can run end to end
- callback-first and polling-fallback paths can both be exercised
- the project can consume `cap4k` from `mavenLocal()`

However, post-delivery review exposed a tactical mismatch between the current project shape and the framework shape that `cap4k` is supposed to dogfood publicly.

The problem is not that the project fails to run. The problem is that it currently proves a weaker tactical path than the one the framework intends to teach.

## Current Problems

The current repository has these high-impact misalignments:

1. HTTP and event entry surfaces do not consistently dogfood `Mediator` as the unified tactical entry.
2. Command persistence does not follow the intended unit-of-work path strongly enough, which led to explicit transaction annotations being added as a repair.
3. Built-in repository families exist but are not demonstrated as first-class tactical surfaces.
4. Aggregate factories are not being used in the real creation path, even though factory generation is an important part of the framework contract.
5. Generated specification artifacts exist in the project without a meaningful tactical role.
6. Adapter HTTP/query boundaries expose application request/response contracts too directly instead of proving `api_payload` boundaries.
7. Handwritten domain-service wiring in the start module is weaker than the reference-project bar.
8. Subscriber/listener naming and entry-shape readability are not strong enough for a quasi-standard reference project.

## Goal

Realign `cap4k-reference-content-studio` so that `v1` once again acts as a valid tactical reference project for `cap4k`.

The corrected repository must still be runnable, but it must also prove the intended tactical shape in code:

- `Mediator` is the unified tactical entry
- repository/factory/service/unit-of-work usage is disciplined by layer
- built-in repository and factory families are actually dogfooded
- generated shells and handwritten completion surfaces have a coherent contract
- adapter boundaries expose payload contracts instead of leaking application contracts
- specification is removed from the active `v1` evidence surface

## Non-Goals

- Do not solve `cap4k#34`, `cap4k#35`, or `cap4k#36` in this slice.
- Do not introduce `only-engine`.
- Do not enable `enumTranslation` before `cap4k#33`.
- Do not expand `v1` into `#23` advanced modeling.
- Do not redesign the scenario itself.
- Do not rewrite `cap4k` framework runtime behavior in this repository.

## Tactical Model To Enforce

### Mediator As The Unified Tactical Entry

The reference project should explicitly dogfood `Mediator` as the public tactical entry.

The project should no longer mix:

- direct handler injection
- direct `RequestSupervisor` injection
- handwritten repository access patterns outside the intended command/query flow

Instead, the tactical contract should be:

- HTTP controllers use `Mediator.cmd` or `Mediator.qry`
- domain and integration subscriber orchestration uses `Mediator.cmd`
- command/query handlers use `Mediator` internally for allowed tactical capabilities

This does not mean every layer can call every mediator surface freely.

### Capability Access Rules By Layer

The capability-usage rule must be explicit in the reference project:

#### Command handlers may use:

- `repo`
- `fac`
- `svc`
- `uow`

#### Command handlers should not use:

- `qry`
- `cli`, except for the bounded case where:
  - the CLI depends on intermediate data produced inside the same command handling flow
  - and the same command also depends on the CLI result to complete its own aggregate write

#### Orchestration surfaces may use:

- `cmd`
- `qry`
- `cli`

This rule comes from the existing public-doc layering model, not from a new ad hoc invention inside the reference repository.

### Layering Source Of Truth

The repository should align with the current `cap4k/docs/public` layering semantics:

- controller / callback bridge / polling job are entry surfaces
- application command/query handlers are aggregate-facing execution surfaces
- domain/integration subscribers are orchestration progression surfaces
- adapter remains boundary and translation territory, not business-truth territory

The realignment work should therefore map current code back to the existing documented model rather than invent a new one.

## Generated Families And Handwritten Completion

### Commands, Queries, And Handlers Remain Generated

This slice does **not** remove generated contracts or generated handler families.

The intended model is:

- `*Cmd` stays generated
- `*Qry` stays generated
- handler families stay generated

But generated handlers must be treated honestly as **skeleton surfaces** rather than as full recurring logic owners.

### Skip-Conflict Skeleton Rule

The following generated families should be treated as `SKIP`-style skeletons in the reference project:

- `CommandHandler`
- `QueryHandler`
- `CliHandler`
- `DomainEventSubscriber`
- `IntegrationEventSubscriber`

That means:

- the family remains part of the generated project shape
- the generated file is still meaningful evidence
- but handwritten completion inside that file remains expected
- the repository must not pretend these files are pure recurring generated shells

This keeps the project aligned with the real generator contract instead of incorrectly pushing project-specific orchestration into a separate fake hand-maintained layer.

## Repository And Factory Dogfooding

### Repository

The project should directly dogfood the built-in repository family.

The current extra layer of handwritten application repository port plus persistence adapter should be removed from the default `v1` tactical evidence line.

The goal is not to remove abstraction for its own sake. The goal is to stop diluting the fact that `cap4k` already provides a real repository family that the reference project is supposed to demonstrate.

The corrected path should prove:

- repository access is part of the `Mediator`-governed tactical model
- built-in repository family is a first-class reference-project surface
- query and command paths do not bypass that contract with project-local repository wrappers

### Factory

Factory remains part of the active `v1` evidence surface and must be used for aggregate-root creation.

Only aggregate roots should have factories in this slice.

In practice this means:

- `Content` creation should go through the generated aggregate factory path
- `MediaProcessingTask` creation should go through the generated aggregate factory path

The reference project must no longer construct these aggregate roots directly in the command handling flow when the framework-generated factory already exists.

## Specification

Specification should be removed from the active `v1` evidence surface.

This is a reference-project cleanup decision, not a framework-contract change.

The current repository does not demonstrate a meaningful tactical role for generated specification artifacts. Keeping them in `v1` as empty or unused evidence lowers the signal quality of the project.

This slice should therefore remove specification from the reference-project contract rather than pretending it already carries meaningful value here.

## Payload Boundary Realignment

Adapter HTTP/query boundaries must stop exposing application request/response contracts too directly.

The corrected shape should be:

- application requests/responses stay internal application contracts
- adapter HTTP requests/responses become explicit `api_payload` surfaces
- controllers translate between payloads and application contracts

This applies especially to:

- query response shape
- create/review request shape
- any HTTP surface currently using application contracts as if they were public adapter contracts

The goal is to make the project prove `api_payload` as a real family rather than as a merely documented capability.

## Domain Service Wiring

`PublicationEligibilityDomainService` remains handwritten in this slice.

This is still compatible with `cap4k#35` being open, because the issue there is a generator-family gap, not the legitimacy of handwritten domain services.

However, the wiring should be corrected:

- use `@DomainService` as the domain-level tactical expression
- do not keep the current start-module `@Bean` registration as the reference-project default shape

This gives the reference project a stronger tactical expression without pretending that `#35` is already solved.

## Naming And Readability

Subscriber and listener naming should become semantic enough to serve as reference material.

The current generic `on(...)` naming is too weak for a project that is meant to teach tactical shape.

The corrected repository should use method names that expose intent clearly, especially for:

- domain subscriber progression
- integration callback progression
- transition surfaces that still exist as handwritten bridges

## Verification Expectations

The corrected slice should be considered complete only if it proves both behavior and tactical shape.

At minimum, verification should include:

- existing happy-path HTTP smoke remains green
- polling fallback proof remains green
- command/query/controller entry tests prove mediator-based execution
- repository/factory dogfood is visible in code shape and regression-tested where practical
- payload-boundary tests prove adapter contracts no longer leak application contracts
- generated snapshot sync still produces coherent evidence after the tactical realignment

## Related Issues

- `cap4k#27` total-goal tracking issue
- `cap4k#33` enum-translation decoupling gate
- `cap4k#34` design-driven integration-event family gap
- `cap4k#35` design-driven domain-service family gap
- `cap4k#36` first-class value-object generation contract gap
- `cap4k#37` public tactical-model and interface-adapter-layering documentation clarification
- `cap4k-reference-content-studio#1` tactical realignment execution issue
