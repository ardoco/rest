# Files

- [Request IDs and result caching](request-ids-and-caching.md) - How ArDoCo REST builds content-based request IDs (TraceLinkType key prefix, project name, and a UUID-from-bytes hash of the concatenated input files), why additionalConfigs is deliberately not hashed, how the 24h Redis TTL keeps key reuse safe, and what this means for API clients.
- [Response and error model](response-and-error-model.md) - The two response schemas of the ArDoCo REST API — the ArdocoResultResponse success envelope (requestId, status, message, traceLinkType, raw-JSON result) and the ErrorResponse body — plus the status/message pairing per endpoint and the GlobalExceptionHandler mapping of file, argument, pipeline, and timeout exceptions to HTTP 422, 500, and 408.
