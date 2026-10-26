---
type: "Reference"
title: "OpenWiki quickstart"
openwiki_generated: true
description: "Entry point for coding agents: one-paragraph system summary, task-routing map into the wiki hierarchy, and the key conventions (request-ID hashing, Redis persistence, async execution) needed before touching code."
tags: [quickstart, overview, ardoco-rest, task-routing]
verified:
  - by: openwiki/0.7.2
    at: 2026-10-26T12:24:12.222Z
sources:
  - id: openwiki-source-17e67da21bbb452f6e1b70e3
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/api_response/TraceLinkType.java
  - id: openwiki-source-a420f4179889786e59957253
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/AbstractController.java
  - id: openwiki-source-3fd9f130687bd87d75f9c8c3
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ArCoTLController.java
  - id: openwiki-source-973f8fea30d6b321ca0bc401
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ArDoCodeController.java
  - id: openwiki-source-8b2a97197949b86658884bf4
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ConfigurationProviderController.java
  - id: openwiki-source-f6d08ba0f56a1c951c2f706a
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/HealthController.java
  - id: openwiki-source-332d4cbd0bf8aa0bbfa82f6b
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/HomeController.java
  - id: openwiki-source-c909e387f9efb6c793afd176
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/InconsistencyController.java
  - id: openwiki-source-868d81e0a1f2a13b4e9c338d
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ResultController.java
  - id: openwiki-source-cad26ad9528b11695a3827dd
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/SwattrController.java
  - id: openwiki-source-0acda492e39a2ad0bb187742
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/TransArCController.java
  - id: openwiki-source-f5df1fa4999bf0efe9f01e6b
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/converter/FileConverter.java
  - id: openwiki-source-ce24a3eb91eb8aac24e67f44
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/repository/CurrentlyRunningRequestsRepository.java
  - id: openwiki-source-3f48285a778da70bb8812331
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/repository/DatabaseAccessor.java
  - id: openwiki-source-ca8e5b1237675b999fd9af3d
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/repository/RedisAccessor.java
  - id: openwiki-source-fbe8e79a4110d3bef05db863
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/AbstractRunnerTLRService.java
  - id: openwiki-source-2984eaa5e09fa7135d8b477a
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/AbstractService.java
  - id: openwiki-source-c7cab41393f5428b55d377f6
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/ConfigurationProviderService.java
  - id: openwiki-source-dcb821af11b2c9f3ac31b92e
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/ResultService.java
  - id: openwiki-source-61ecae979de37af2235b7f98
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/util/HashGenerator.java
  - id: openwiki-source-b2cbaede3bd44199cffd04ad
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/ArDoCoRestApplication.java
  - id: openwiki-source-ece093ad10b91f3da0b7e34f
    resource: repo://src/main/resources/application.properties
generated: { by: "openwiki/0.7.2", at: "2026-10-26T12:24:12.222Z" }
---

# OpenWiki quickstart

ArDoCo REST is a Spring Boot application — entrypoint
`edu.kit.kastel.mcse.ardoco.tlr.rest.ArDoCoRestApplication`, annotated with `@EnableAsync` and
`@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)` — that exposes the ArDoCo
trace-link recovery pipelines over HTTP. It offers four runner endpoint families, each with its own
controller under `/api` (ArDoCode/sad-code at `/api/ardocode`, ArCoTL/sam-code at `/api/arcotl`,
SWATTR/sad-sam at `/api/swattr`, TransArC/sad-sam-code at `/api/transarc`) plus an
inconsistency-detection workflow at `/api/find-inconsistencies` and a shared
<!-- openwiki: broken internal link [src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ResultController.java] file "src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ResultController.java" does not exist. Fix the href or restore the target, then delete this comment. -->
[ResultController](src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ResultController.java)
for `/api/get-result/{id}` and `/api/wait-for-result/{id}`. Each start endpoint accepts multipart
uploads, converts them to temporary files, derives a content-hash request ID, and hands a pre-built
ArDoCo runner to the service layer, which runs the pipeline asynchronously on a
`CompletableFuture` unless the result is already cached or in flight; results are stored as raw JSON
in Redis (the `@Primary` `RedisAccessor` behind the `DatabaseAccessor` interface) with a 24-hour TTL
and retrieved later through the shared result endpoints with 200/202 semantics.

## What this repository does

The application provides REST access to several ArDoCo trace-link recovery workflows:

- **ArDoCode / sad-code** (`SAD_CODE`, prefix `ardocodeResult`): textual documentation + code
- **ArCoTL / sam-code** (`SAM_CODE`, prefix `arcotlResult`): architecture model + code
- **SWATTR / sad-sam** (`SAD_SAM`, prefix `swattrResult`): textual documentation + architecture model
- **TransArC / sad-sam-code** (`SAD_SAM_CODE`, prefix `transarcResult`): combined architecture, documentation, and code workflow

<!-- openwiki: broken internal link [src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/api_response/TraceLinkType.java] file "src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/api_response/TraceLinkType.java" does not exist. Fix the href or restore the target, then delete this comment. -->
The prefixes live in the [`TraceLinkType`](src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/api_response/TraceLinkType.java)
enum and double as Redis key prefixes. Auxiliary endpoints: `/api/configuration` (reflection-derived
ArDoCo default configuration), `/api/health`, and `/` which redirects to the Swagger UI at
`http://localhost:8080/swagger-ui/index.html`; actuator health is exposed at
`http://localhost:8080/actuator/health`.

## Repository shape

The codebase is organized around the standard controller → service → repository flow:

- `src/main/java/.../controller/` contains the HTTP entrypoints; controllers build the runners themselves
- `src/main/java/.../service/` orchestrates runner execution (`AbstractRunnerTLRService`), result lookup and waiting (`ResultService`)
- `src/main/java/.../repository/` wraps Redis access (`DatabaseAccessor`/`RedisAccessor`) and the in-memory registry of in-flight requests (`CurrentlyRunningRequestsRepository`)
- `src/main/java/.../converter/` and `.../api_response/` handle file conversion and response shaping
- `src/test/java/.../` contains controller, Redis, utility, and architecture tests

The repository also includes:

- `README.md` for a short manual run note
- `HELP.md` for Spring Boot and Docker hints
- `architecture_decisions.md` for the original architecture rationale and response schema examples
- `.github/workflows/openwiki.yml` for the automated docs update workflow

## How the API behaves

Most endpoints follow the same pattern:

1. Accept multipart inputs and optional JSON configuration (`additionalConfigs`).
2. Convert uploaded files to temporary local files.
3. Generate a request ID from the files, project name, and trace-link type.
4. Set up the appropriate ArDoCo runner.
5. Launch the pipeline asynchronously if the result is not already cached or in flight.
6. Return either an in-progress response, a ready result, or an accepted/timed-out response depending on the endpoint.

Result data is stored in Redis with a time-to-live configured in the application environment
(`redis.time-to-live.hours`, 24 by default).

## Important conventions

- **Request IDs are content-based, config-free.** The ID is
  `traceLinkType.getKeyPrefix() + ":" + projectName + hash`, where `hash` is
  `UUID.nameUUIDFromBytes` over the concatenated bytes of all input files. The optional
  `additionalConfigs` JSON is parsed into a sorted map for the runner but deliberately *not* hashed:
  changing only the configuration yields the same request ID and therefore hits the cached result.
- **Redis is the canonical persistence/cache layer.** Everything is stored as key → raw-JSON result;
  entries expire after the configured TTL. Implementations sit behind the `DatabaseAccessor`
  interface so the backend can be swapped.
- **Execution is asynchronous at the ArDoCo boundary only.** File conversion and runner setup happen
  synchronously in the controller so clients get fast feedback on bad input; only
  `runner.run()` runs on a `CompletableFuture` tracked in the
  `CurrentlyRunningRequestsRepository` (a singleton `ConcurrentHashMap`). When the pipeline fails,
  an `"Error: "`-prefixed payload is stored in Redis and later rethrown to clients as
  `ArdocoException`; the in-flight entry and the temporary input files are always cleaned up.
- **Two response schemas and a 202 convention.** Start endpoints return an
  `ArdocoResultResponse` envelope (`requestId`/`status`/`message`/`traceLinkType`/`result`);
  retrieval returns 200 with results, 202 while pending or after the `tlr.timeout.seconds` (60 s)
  wait window expires, and errors surface via the centralized `GlobalExceptionHandler`.
- **Temporary files are created during request handling and cleaned up after pipeline execution.**

## Task-routing map

Start here if you need to understand the repository quickly:

- [Architecture](architecture.md) — layered design, controller/service inheritance hierarchies, and the split between in-flight tracking and durable Redis storage
- [Runner domains and API surface](domains.md) — the four trace-link families, endpoints per family, required multipart inputs, and the model-format parameter
- [Workflow: pipeline execution](workflows/pipeline-execution.md) — end-to-end control flow of starting a pipeline, dedup, async run, and the error path
- [Workflow: result retrieval and waiting](workflows/result-retrieval.md) — the four retrieval modes, timeout behavior, and the 202 convention
- [Request IDs and result caching](concepts/request-ids-and-caching.md) — how IDs are constructed, why configs are not hashed, TTL, and dedup consequences
- [Response and error model](concepts/response-and-error-model.md) — both response schemas and the exception-to-HTTP-status mapping
- [ArDoCo runner integration](integrations/ardoco-runners.md) — how the REST layer binds to the external ArDoCo runner classes and configurations
- [Redis persistence integration](integrations/redis.md) — the persistence boundary, Lettuce connection setup, and TTL configuration
- [Operations](operations.md) — how to build, run, deploy, and observe the service
- [Testing](testing.md) — what the test suites cover and what to run after changes

## If you are changing the code

Use the docs above as the map, then verify changes in the relevant layer:

- API or controller changes (parameters, endpoints, response statuses): [Runner domains and API surface](domains.md) and [Response and error model](concepts/response-and-error-model.md)
- Request-ID, hashing, or caching semantics: [Request IDs and result caching](concepts/request-ids-and-caching.md)
- Runner/service changes (async behavior, dedup, cleanup paths): [Workflow: pipeline execution](workflows/pipeline-execution.md), plus [ArDoCo runner integration](integrations/ardoco-runners.md) for runner binding details
- Retrieval, polling, or timeout behavior: [Workflow: result retrieval and waiting](workflows/result-retrieval.md)
- Redis or persistence changes: check TTL behavior and the `DatabaseAccessor` abstraction via [Redis persistence integration](integrations/redis.md)
- Layering/structure invariants: [Architecture](architecture.md), enforced partly by `ArchitectureTest`
- Operations changes (build, Docker, CI): update [Operations](operations.md) and `HELP.md` if run instructions change
- Tests: see [Testing](testing.md) for the relevant suites

## Source references

- `pom.xml`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/ArDoCoRestApplication.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/AbstractController.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ArDoCodeController.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ArCoTLController.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/InconsistencyController.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/AbstractService.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/AbstractRunnerTLRService.java`
- `src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/repository/RedisAccessor.java`
- `src/test/java/edu/kit/kastel/mcse/ardoco/tlr/rest/ArchitectureTest.java`
