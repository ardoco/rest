---
okf_version: "0.2"
---

# Files

- [Architecture](architecture.md) - Layered controller, service, and repository design of the ArDoCo REST API, covering the AbstractController, AbstractService, and AbstractRunnerTLRService hierarchies, runner setup in controllers, and the split between in-flight tracking and durable Redis storage.
- [Domains](domains.md) - The five trace-link recovery and inconsistency-detection domains of ArDoCo REST: endpoint paths, required multipart inputs, TraceLinkType prefixes, runner classes, and the payload shape each family returns.
- [Operations](operations.md) - How to build, run, deploy, and observe ArDoCo REST: Maven builds on the ardoco parent POM with the Spring Boot BOM and JUnit overrides, the README local run flow, the docker-compose redis/redisinsight/app topology, the multi-stage Dockerfile and Spring profiles, and the GitHub Actions CI, docker-publish, OpenWiki, and Dependabot automations.
- [OpenWiki quickstart](quickstart.md) - Entry point for coding agents: one-paragraph system summary, task-routing map into the wiki hierarchy, the key conventions (request-ID hashing, Redis persistence, async execution), and the generated-wiki maintenance note needed before touching code.
- [Testing](testing.md) - What the ArDoCo REST test suites cover and what to run after a change: the Testcontainers-based controller integration tests per runner family (AbstractTLRControllerTest plus subclasses), the RedisAccessor container tests and the Testcontainers Redis pattern, the utility unit tests (FileConverter, HashGenerator, TraceLinkConverter), the ArchUnit ArchitectureTest rule, and the change-sensitive areas.

# Directories

- [concepts](concepts/)
- [integrations](integrations/)
- [workflows](workflows/)
