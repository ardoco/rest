---
type: "Reference"
title: "OpenWiki quickstart"
openwiki_generated: true
description: "Entry point for coding agents: one-paragraph system summary, task-routing map into the wiki hierarchy, the key conventions (request-ID hashing, Redis persistence, async execution, CORS origin allowlist), and the generated-wiki maintenance note needed before touching code."
tags: [quickstart, overview, ardoco-rest, task-routing, cors]
sources:
  - id: openwiki-source-6d3ac2bdfb0e76882a670989
    resource: repo://.github/workflows/openwiki.yml
  - id: openwiki-source-8037e2358a2c4f9b2c722a11
    resource: repo://AGENTS.md
  - id: openwiki-source-2355f81d7cf522f8dbdaabd4
    resource: repo://pom.xml
  - id: openwiki-source-23775c3de52f3ab95a13cb8b
    resource: repo://README.md
  - id: openwiki-source-db53f3b7825ef9712a7964e1
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/api_response/ArdocoResultResponse.java
  - id: openwiki-source-17e67da21bbb452f6e1b70e3
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/api_response/TraceLinkType.java
  - id: openwiki-source-a420f4179889786e59957253
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/AbstractController.java
  - id: openwiki-source-3fd9f130687bd87d75f9c8c3
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ArCoTLController.java
  - id: openwiki-source-973f8fea30d6b321ca0bc401
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/controller/ArDoCodeController.java
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
  - id: openwiki-source-1c5a7ae3754c990399f4ebd1
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/converter/TraceLinkConverter.java
  - id: openwiki-source-5234cb96d26784ace8285f29
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/exception/GlobalExceptionHandler.java
  - id: openwiki-source-e88bcc81194c6ad9e9618bc0
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/messages/ResultMessages.java
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
  - id: openwiki-source-dcb821af11b2c9f3ac31b92e
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/service/ResultService.java
  - id: openwiki-source-61ecae979de37af2235b7f98
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/api/util/HashGenerator.java
  - id: openwiki-source-b2cbaede3bd44199cffd04ad
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/ArDoCoRestApplication.java
  - id: openwiki-source-bd27e22b24a569b1300c8892
    resource: repo://src/main/java/edu/kit/kastel/mcse/ardoco/tlr/rest/WebConfig.java
  - id: openwiki-source-c8948fffa3abb80f9c097fd5
    resource: repo://src/main/resources/application-local.properties
  - id: openwiki-source-ece093ad10b91f3da0b7e34f
    resource: repo://src/main/resources/application.properties
generated: { by: "openwiki/0.9.0", at: "2026-11-23T17:33:36.087Z" }
verified:
  - by: openwiki/0.9.0
    at: 2026-11-23T17:33:36.087Z
---

# OpenWiki quickstart

ArDoCo REST is a Spring Boot application — entrypoint
`edu.kit.kastel.mcse.ardoco.tlr.rest.ArDoCoRestApplication`, annotated with `@EnableAsync` and
`@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })` — that exposes the ArDoCo
trace-link recovery and inconsistency-detection pipelines over HTTP. It offers four TLR runner
families, each with its own controller under `/api` (ArDoCode/sad-code at `/api/ardocode`,
ArCoTL/sam-code at `/api/arcotl`, SWATTR/sad-sam at `/api/swattr`, TransArC/sad-sam-code at
`/api/transarc`), an inconsistency-detection workflow at `/api/find-inconsistencies`, and a shared
`ResultController` for `/api/get-result/{id}` and `/api/wait-for-result/{id}`. Each start endpoint
accepts multipart uploads, converts them to temporary files, derives a content-hash request ID, and
hands a pre-built ArDoCo runner to the service layer, which runs the pipeline asynchronously on a
`CompletableFuture` unless the result is already cached or in flight; results are stored as raw JSON
in Redis (the `@Primary` `RedisAccessor` behind the `DatabaseAccessor` interface) with a 24-hour TTL
and retrieved later through the shared result endpoints with 200/202 semantics.

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
- **Two response schemas and a 200/202 convention.** Start endpoints return an
  `ArdocoResultResponse` envelope (`requestId`/`status`/`message`/`traceLinkType`/`result`) — plain
  start endpoints answer 200 immediately with a processing or ready message, while start-and-wait
  endpoints block and answer 202 with a timeout message once the `tlr.timeout.seconds` (60 s) wait
  window expires; retrieval returns 200 with results and 202 while pending, and errors surface via
  the centralized `GlobalExceptionHandler` (422 for bad input, 500 for pipeline errors, 408 for
  timeouts).
- **Stored results are re-parsed on retrieval.** `AbstractService.getResultFromDatabase` reads the
  raw-JSON string back and re-parses it with `JsonMapper.readTree`, manually extracting the
  `traceLinks` and `inconsistencies` arrays into the `ArDoCoApiResult` DTO, so the payload shape of
  `ArDoCoApiResult` is defined by the converter-produced JSON, not by bean deserialization.
- **Jackson 3 with legacy annotations on response DTOs.** JSON handling uses `tools.jackson`
  (Jackson 3): controllers and services inject `tools.jackson.databind.json.JsonMapper`, converters
  use `JsonMapper.shared()`, and `AbstractController.parseAdditionalConfigs` catches
  `tools.jackson.core.JacksonException` when parsing `additionalConfigs`. The response DTOs
  (`ArdocoResultResponse`, `ArDoCoApiResult`, `ErrorResponse`) still carry
  `com.fasterxml.jackson.annotation` annotations, which Jackson 3 retains for compatibility.
- **Temporary files are created during request handling and cleaned up after pipeline execution.**
- **Browser access is gated by a CORS origin allowlist.** `WebConfig` reads `cors.allowed-origins`
  (comma-separated; default `https://ardoco.de`, override via the `CORS_ALLOWED_ORIGINS`
  environment variable) and registers a single CORS mapping for `/api/**` that allows only `GET`
  and `POST` with credentials disabled; an empty value disables CORS entirely. The `local` profile
  additionally allows `http://localhost:3000` and `http://localhost:8000` for a locally served web
  frontend.

## Task-routing map

Start here if you need to understand the repository quickly. The complete committed wiki is listed
in [index.md](index.md); the `concepts/`, `integrations/`, and `workflows/` directories each carry
their own index page.

- [Architecture](architecture.md) — the layered controller → service → repository design; start here for layering invariants and where responsibilities and state live
- [Runner domains and API surface](domains.md) — the runner families, endpoints, inputs, and payload differences; start here for any HTTP-surface change
- [Workflow: pipeline execution](workflows/pipeline-execution.md) — start-to-finish control flow of a run, including dedup and the error path
- [Workflow: result retrieval and waiting](workflows/result-retrieval.md) — the retrieval modes, wait budget, and the 202 convention
- [Request IDs and result caching](concepts/request-ids-and-caching.md) — how IDs are built, why configs are not hashed, and the resulting dedup and TTL semantics
- [Response and error model](concepts/response-and-error-model.md) — both response schemas and the exception-to-HTTP-status mapping
- [ArDoCo runner integration](integrations/ardoco-runners.md) — how the REST layer binds to the external ArDoCo runner classes and configurations
- [Redis persistence integration](integrations/redis.md) — the persistence boundary, connection setup, and TTL configuration
- [Operations](operations.md) — building, running, deploying, and observing the service, including the Renovate-based dependency automation
- [Testing](testing.md) — which suites cover what and what to run after a change, including the `WebConfigCorsTest` MockMvc preflight suite

## If you are changing the code

Use the routing map above, then verify changes in the relevant layer:

- API or controller changes (parameters, endpoints, response statuses): [Runner domains and API surface](domains.md) and [Response and error model](concepts/response-and-error-model.md)
- Request-ID, hashing, or caching semantics: [Request IDs and result caching](concepts/request-ids-and-caching.md)
- Runner/service changes (async behavior, dedup, cleanup paths): [Workflow: pipeline execution](workflows/pipeline-execution.md), plus [ArDoCo runner integration](integrations/ardoco-runners.md) for runner binding details
- Retrieval, polling, or timeout behavior: [Workflow: result retrieval and waiting](workflows/result-retrieval.md)
- Redis or persistence changes: check TTL behavior and the `DatabaseAccessor` abstraction via [Redis persistence integration](integrations/redis.md)
- JSON handling or response DTO changes: [Response and error model](concepts/response-and-error-model.md) and the stored-result re-parse path in [Workflow: result retrieval](workflows/result-retrieval.md)
- CORS or browser-access changes: the CORS convention above, the configuration details in [Operations](operations.md), and the preflight suite in [Testing](testing.md)
- Layering/structure invariants: [Architecture](architecture.md), enforced partly by `ArchitectureTest`
- Operations changes (build, Docker, CI, Renovate automation): update [Operations](operations.md) and `HELP.md` if run instructions change
- Tests: see [Testing](testing.md) for the relevant suites
- Wiki pages: they are generated — see [Wiki maintenance](#wiki-maintenance) before editing anything under `openwiki/`

## Wiki maintenance

This wiki (the `openwiki/` directory) is generated documentation, not hand-written prose. Per
[AGENTS.md](../AGENTS.md), the repository's documented maintenance process refreshes generated pages
after source changes — automated weekly by `.github/workflows/openwiki.yml` — so prefer updating
source code or docs and letting OpenWiki regenerate rather than hand-editing these pages. Two
practical consequences for agents working here:

- Treat source code and tests as authoritative when they disagree with a wiki page; a stale page is a verification gap, not ground truth.
- When pages move or are added, keep this quickstart's routing map and change-guide links pointing at every committed page.
