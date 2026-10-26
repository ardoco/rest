# Files

- [ArDoCo runner integration](ardoco-runners.md) - How the REST layer binds to the external ArDoCo libraries (pipeline-id, pipeline-tlr, text-preprocessing) — the runner classes Ardocode, Arcotl, Swattr, Transarc and InconsistencyDetection, their setUp signatures and configuration objects, ModelFormat conversion, throwaway temp output directories, and the reflection-based /api/configuration default-config endpoint.
- [Redis persistence integration](redis.md) - The persistence boundary of ArDoCo REST — the DatabaseAccessor interface, the @Primary RedisAccessor writing TTL-bound plain-string values, the Lettuce connection setup in ApplicationConfig, the docker/local Spring profile host split, and the local docker-compose and RedisInsight tooling.
