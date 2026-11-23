# ArDoCo REST
Details concerning the architectural decisions as well as the API responses can be found [here](architecture_decisions.md).

Remark: There also exists another branch (`extra-result-controller`) which introduces a new controller to handle the retrieval of the TraceLinks separate
from starting the pipeline. The branch also contains a ``.md`` file explaining the approach further.

## how to run the API

1. The API uses a Redis database. Add the credentials to ``redis/redis_template.env`` and rename the file to ``redis/redis.env``
2. Start redis in docker from the docker-compose.yaml (``docker-compose up -d``)
3. Start the application
4. To access the API through the ui provided by swagger, go to http://localhost:8080/swagger-ui/index.html
5. The contents of the database can be accessed through http://localhost:5540

## CORS configuration

Browser clients (e.g. the TraceViz web demo) may only call ``/api/**`` from origins listed in the property ``cors.allowed-origins``
(comma-separated, only ``GET`` and ``POST``, no credentials). The default is ``https://ardoco.de``; an empty value disables CORS.
Override it via the environment variable ``CORS_ALLOWED_ORIGINS`` (Spring relaxed binding), e.g. ``CORS_ALLOWED_ORIGINS=https://ardoco.de,https://example.org``
in the ``environment`` list of the ``app`` service in ``docker-compose.yaml``.

The ``local`` profile additionally allows some localhost origins.
