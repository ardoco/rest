# Files

- [Workflow: pipeline execution](pipeline-execution.md)
- [Workflow: result retrieval and waiting](result-retrieval.md) - How a finished pipeline result is retrieved by request ID: the non-blocking GET /api/get-result/{id}, the bounded-blocking GET /api/wait-for-result/{id}, and the start-and-wait flow, all funnelling through ResultService with a 200 OK / 202 Accepted convention and distinct 422/500 error paths.
